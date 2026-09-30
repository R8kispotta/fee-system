package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Student
import com.example.data.model.InstituteSettings
import com.example.data.model.MonthFeeStatus
import com.example.data.model.StudentMonthlyFeeRecord
import com.example.ui.components.StudentAvatar
import com.example.ui.components.WhatsAppHelper
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PendingAmber
import com.example.ui.theme.WhatsAppColor

enum class WhatsAppCategory(val label: String, val icon: String) {
    OVERDUE("Overdue", "🚨"),
    UPCOMING("Upcoming", "🔔"),
    LIFECYCLE("Enrollment", "🎓")
}

@Composable
fun WhatsAppHubScreen(
    records: List<StudentMonthlyFeeRecord>,
    settings: InstituteSettings,
    onSendAiCustomReminder: (StudentMonthlyFeeRecord) -> Unit,
    onEnrollmentNotification: (Student) -> Unit,
    onWithdrawalNotification: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(WhatsAppCategory.OVERDUE) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf("All") }
    var sendToTarget by remember { mutableStateOf("Parent First") } // "Parent First", "Student"
    var isBannerExpanded by remember { mutableStateOf(false) }

    // Session sent state tracking
    val sentStatusMap = remember { mutableStateMapOf<Long, Boolean>() }

    val activeRecords = remember(records) {
        records.filter { it.student.enrollmentStatus == "ACTIVE" }
    }

    val overdueRecords = remember(activeRecords) {
        activeRecords.filter { it.status == MonthFeeStatus.OVERDUE }
    }

    val upcomingRecords = remember(activeRecords) {
        activeRecords.filter { it.status == MonthFeeStatus.PENDING || it.status == MonthFeeStatus.PARTIALLY_PAID }
    }

    val batches = remember(records) {
        listOf("All") + records.map { it.student.batch }.distinct()
    }

    val displayedList = remember(selectedCategory, overdueRecords, upcomingRecords, activeRecords, searchQuery, selectedBatchFilter) {
        val baseList = when (selectedCategory) {
            WhatsAppCategory.OVERDUE -> overdueRecords
            WhatsAppCategory.UPCOMING -> upcomingRecords
            WhatsAppCategory.LIFECYCLE -> activeRecords
        }

        baseList.filter { record ->
            val matchesQuery = searchQuery.isBlank() ||
                    record.student.name.contains(searchQuery, ignoreCase = true) ||
                    record.student.phone.contains(searchQuery) ||
                    record.student.parentPhone.contains(searchQuery)

            val matchesBatch = selectedBatchFilter == "All" || record.student.batch == selectedBatchFilter

            matchesQuery && matchesBatch
        }
    }

    val totalInCurrentCategory = displayedList.size
    val sentCount = displayedList.count { sentStatusMap[it.student.id] == true }
    val nextPendingRecord = displayedList.firstOrNull { sentStatusMap[it.student.id] != true }

    fun resolvePhone(student: Student): String {
        return if (sendToTarget == "Parent First" && student.parentPhone.isNotEmpty()) {
            student.parentPhone
        } else {
            student.phone
        }
    }

    fun dispatchMessageForRecord(record: StudentMonthlyFeeRecord) {
        val phone = resolvePhone(record.student)
        val dueDateStr = "${record.student.dueDay}th of ${record.monthYear}"
        val message = if (record.isOverdue) {
            WhatsAppHelper.buildOverdueFeeReminder(
                student = record.student,
                monthYear = record.monthYear,
                amountDue = record.amountDue,
                dueDate = dueDateStr,
                daysOverdue = record.daysOverdue,
                settings = settings
            )
        } else {
            WhatsAppHelper.buildUpcomingFeeReminder(
                student = record.student,
                monthYear = record.monthYear,
                amountDue = record.amountDue,
                dueDate = dueDateStr,
                settings = settings
            )
        }
        WhatsAppHelper.sendWhatsAppMessage(context, phone, message)
        sentStatusMap[record.student.id] = true
    }

    Column(modifier = modifier.fillMaxSize()) {
        // --- 1. Compact Sticky Tab Row with Badges (Only 36dp high) ---
        ScrollableTabRow(
            selectedTabIndex = selectedCategory.ordinal,
            edgePadding = 8.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = WhatsAppColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            WhatsAppCategory.values().forEach { cat ->
                val count = when (cat) {
                    WhatsAppCategory.OVERDUE -> overdueRecords.size
                    WhatsAppCategory.UPCOMING -> upcomingRecords.size
                    WhatsAppCategory.LIFECYCLE -> activeRecords.size
                }
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(cat.icon, fontSize = 11.sp)
                            Text(
                                text = "${cat.label} ($count)",
                                fontSize = 11.sp,
                                fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.height(36.dp)
                )
            }
        }

        // --- 2. Horizontal Scrollable Options Bar (Hub Info Toggle, Target, Batches) ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = isBannerExpanded,
                    onClick = { isBannerExpanded = !isBannerExpanded },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (isBannerExpanded) WhatsAppColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text(if (isBannerExpanded) "Hide Hub Info" else "Hub Info", fontSize = 10.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }

            item {
                FilterChip(
                    selected = sendToTarget == "Parent First",
                    onClick = {
                        sendToTarget = if (sendToTarget == "Parent First") "Student" else "Parent First"
                    },
                    label = { Text("To: $sendToTarget", fontSize = 10.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }

            items(batches) { b ->
                FilterChip(
                    selected = selectedBatchFilter == b,
                    onClick = { selectedBatchFilter = b },
                    label = { Text(b, fontSize = 10.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // --- 3. Collapsible Banner (Animated, Only when toggled) ---
        AnimatedVisibility(
            visible = isBannerExpanded,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(250)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppColor.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WhatsApp Automation Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "1-tap official fee alerts, payment receipts, AI customized notices, and enrollment letters via WhatsApp/SMS.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 13.sp
                        )
                    }
                    IconButton(
                        onClick = { isBannerExpanded = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // --- 4. Scrollable Workspace (Search & Student List) ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Automated Sequence Bar (Compact Single Card)
            if (selectedCategory != WhatsAppCategory.LIFECYCLE && displayedList.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Queue: $sentCount of $totalInCurrentCategory sent",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )

                                if (nextPendingRecord != null) {
                                    Button(
                                        onClick = { dispatchMessageForRecord(nextPendingRecord) },
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("send_next_whatsapp_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppColor),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Send: ${nextPendingRecord.student.name.take(12)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                } else if (sentCount > 0 && sentCount == totalInCurrentCategory) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PaidGreen, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Batch Done!", fontSize = 10.sp, color = PaidGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = {
                                    if (totalInCurrentCategory > 0) sentCount.toFloat() / totalInCurrentCategory.toFloat() else 0f
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = WhatsAppColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Compact Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search student or phone...", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(15.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(13.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            // Student Reminder Cards
            if (displayedList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PaidGreen, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (selectedCategory) {
                                    WhatsAppCategory.OVERDUE -> "No overdue dues! All accounts clear."
                                    WhatsAppCategory.UPCOMING -> "No upcoming dues found."
                                    WhatsAppCategory.LIFECYCLE -> "No students matching filter."
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayedList, key = { it.student.id }) { record ->
                    val isSent = sentStatusMap[record.student.id] == true
                    val student = record.student
                    val targetPhone = resolvePhone(student)

                    WhatsAppCompactCard(
                        record = record,
                        targetPhone = targetPhone,
                        isSent = isSent,
                        settings = settings,
                        category = selectedCategory,
                        onSendNow = { dispatchMessageForRecord(record) },
                        onSendAiReminder = { onSendAiCustomReminder(record) },
                        onSendEnrollment = { onEnrollmentNotification(record.student) },
                        onSendWithdrawal = { onWithdrawalNotification(record.student) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatsAppCompactCard(
    record: StudentMonthlyFeeRecord,
    targetPhone: String,
    isSent: Boolean,
    settings: InstituteSettings,
    category: WhatsAppCategory,
    onSendNow: () -> Unit,
    onSendAiReminder: () -> Unit,
    onSendEnrollment: () -> Unit,
    onSendWithdrawal: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSent) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(name = record.student.name, photoUri = record.student.photoUri, size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        if (isSent) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PaidGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Sent ✅",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PaidGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${record.student.gradeClass} • ${record.student.batch}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "📱 WhatsApp: $targetPhone",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Financial status
                Column(horizontalAlignment = Alignment.End) {
                    if (category != WhatsAppCategory.LIFECYCLE) {
                        Text(
                            text = "${settings.currencySymbol}${record.amountDue.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp,
                            color = if (record.isOverdue) OverdueRed else PendingAmber
                        )
                        Text(
                            text = if (record.isOverdue) "${record.daysOverdue}d Overdue" else "Due ${record.student.dueDay}th",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (record.isOverdue) OverdueRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (record.student.enrollmentStatus == "ACTIVE") PaidGreen.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = record.student.enrollmentStatus,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (record.student.enrollmentStatus == "ACTIVE") PaidGreen else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons (Ultra-compact & high-touch)
            if (category == WhatsAppCategory.LIFECYCLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onSendEnrollment,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Welcome", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onSendWithdrawal,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Exit Notice", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onSendNow,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSent) "Send Again" else "1-Tap WhatsApp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onSendAiReminder,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("AI Tone", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
