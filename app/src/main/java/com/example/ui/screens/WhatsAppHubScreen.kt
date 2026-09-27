package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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

enum class WhatsAppCategory(val title: String) {
    OVERDUE("🚨 Overdue Dues"),
    UPCOMING("🔔 Upcoming Fees"),
    LIFECYCLE("🎓 Enrollment & Exit")
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

    // Track which students have been messaged in the current session
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

    // Automated Sequence Progress
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // WhatsApp Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppColor.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = WhatsAppColor,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WhatsApp Automation Hub",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Send automated fee reminders, welcome letters & exit notices",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Tabs (Overdue, Upcoming, Lifecycle)
        TabRow(
            selectedTabIndex = selectedCategory.ordinal,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            WhatsAppCategory.values().forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    text = {
                        Text(
                            text = cat.title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Automated Dispatch Queue Bar (for Overdue & Upcoming)
        if (selectedCategory != WhatsAppCategory.LIFECYCLE && displayedList.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Automated Dispatch Sequence",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "$sentCount of $totalInCurrentCategory sent in this session",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Recipient Target Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = sendToTarget == "Parent First",
                                onClick = { sendToTarget = "Parent First" },
                                label = { Text("Parent", fontSize = 10.sp) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChip(
                                selected = sendToTarget == "Student",
                                onClick = { sendToTarget = "Student" },
                                label = { Text("Student", fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = {
                            if (totalInCurrentCategory > 0) sentCount.toFloat() / totalInCurrentCategory.toFloat() else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = WhatsAppColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    if (nextPendingRecord != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { dispatchMessageForRecord(nextPendingRecord) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_next_whatsapp_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Send Next: ${nextPendingRecord.student.name} (${settings.currencySymbol}${nextPendingRecord.amountDue.toInt()})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else if (sentCount > 0 && sentCount == totalInCurrentCategory) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PaidGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All reminders in this category dispatched!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PaidGreen
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Search & Filter controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student or mobile...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Batch Filter Chips if multiple
        if (batches.size > 2) {
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(batches) { b ->
                    FilterChip(
                        selected = selectedBatchFilter == b,
                        onClick = { selectedBatchFilter = b },
                        label = { Text(b, fontSize = 11.sp) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Student Reminder Roster
        if (displayedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PaidGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (selectedCategory) {
                            WhatsAppCategory.OVERDUE -> "No overdue payments! All accounts clear."
                            WhatsAppCategory.UPCOMING -> "No upcoming pending fees found."
                            WhatsAppCategory.LIFECYCLE -> "No students match search filter."
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedList, key = { it.student.id }) { record ->
                    val isSent = sentStatusMap[record.student.id] == true
                    val student = record.student
                    val targetPhone = resolvePhone(student)

                    WhatsAppStudentCard(
                        record = record,
                        targetPhone = targetPhone,
                        isSent = isSent,
                        settings = settings,
                        category = selectedCategory,
                        onSendNow = {
                            dispatchMessageForRecord(record)
                        },
                        onSendAiReminder = {
                            onSendAiCustomReminder(record)
                        },
                        onSendEnrollment = {
                            onEnrollmentNotification(record.student)
                        },
                        onSendWithdrawal = {
                            onWithdrawalNotification(record.student)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatsAppStudentCard(
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSent) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(name = record.student.name, photoUri = record.student.photoUri, size = 46.dp)
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (isSent) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PaidGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Sent ✅",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PaidGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${record.student.gradeClass} • ${record.student.batch}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "📱 WhatsApp to: $targetPhone",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Financial badge or status
                Column(horizontalAlignment = Alignment.End) {
                    if (category != WhatsAppCategory.LIFECYCLE) {
                        Text(
                            text = "${settings.currencySymbol}${record.amountDue.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (record.isOverdue) OverdueRed else PendingAmber
                        )
                        Text(
                            text = if (record.isOverdue) "${record.daysOverdue}d Overdue" else "Due on ${record.student.dueDay}th",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (record.isOverdue) OverdueRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (record.student.enrollmentStatus == "ACTIVE") PaidGreen.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = record.student.enrollmentStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (record.student.enrollmentStatus == "ACTIVE") PaidGreen else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            if (category == WhatsAppCategory.LIFECYCLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSendEnrollment,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Welcome Notice", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onSendWithdrawal,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Exit / Withdrawal", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSendNow,
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppColor),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSent) "Send Again" else "Send Reminder",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onSendAiReminder,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Custom", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
