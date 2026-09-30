package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FeePayment
import com.example.data.local.Student
import com.example.data.model.MonthFeeStatus
import com.example.data.model.StudentMonthlyFeeRecord
import com.example.ui.components.MonthStatusBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.components.WhatsAppHelper
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.WhatsAppColor

@Composable
fun StudentsScreen(
    records: List<StudentMonthlyFeeRecord>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedStatusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    selectedClassFilter: String,
    onClassFilterChange: (String) -> Unit,
    currencySymbol: String,
    onAddStudentClick: () -> Unit,
    onStudentClick: (Student) -> Unit,
    onEditStudentClick: (Student) -> Unit,
    onDeleteStudentClick: (Student) -> Unit,
    onCollectFeeClick: (Student) -> Unit,
    onViewReceiptClick: (FeePayment) -> Unit,
    onAiReminderClick: (StudentMonthlyFeeRecord) -> Unit,
    onSendEnrollmentClick: (Student) -> Unit = {},
    onWithdrawStudentClick: (Student) -> Unit = {},
    onManageBatchesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusFilters = listOf("All", "Pending", "Overdue", "Paid", "Withdrawn")

    // Extract all distinct classes
    val availableClasses = remember(records) {
        listOf("All") + records.map { it.student.gradeClass }.distinct()
    }

    // Filter students
    val filteredRecords = remember(records, searchQuery, selectedStatusFilter, selectedClassFilter) {
        records.filter { record ->
            val matchesQuery = searchQuery.isBlank() ||
                    record.student.name.contains(searchQuery, ignoreCase = true) ||
                    record.student.phone.contains(searchQuery) ||
                    record.student.rollNo.contains(searchQuery, ignoreCase = true)

            val matchesStatus = when (selectedStatusFilter) {
                "Paid" -> record.status == MonthFeeStatus.PAID && record.student.enrollmentStatus == "ACTIVE"
                "Pending" -> (record.status == MonthFeeStatus.PENDING || record.status == MonthFeeStatus.PARTIALLY_PAID) && record.student.enrollmentStatus == "ACTIVE"
                "Overdue" -> record.status == MonthFeeStatus.OVERDUE && record.student.enrollmentStatus == "ACTIVE"
                "Withdrawn" -> record.student.enrollmentStatus == "WITHDRAWN"
                else -> true
            }

            val matchesClass = selectedClassFilter == "All" || record.student.gradeClass == selectedClassFilter

            matchesQuery && matchesStatus && matchesClass
        }
    }

    var isSearchVisible by remember { mutableStateOf(searchQuery.isNotEmpty()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("add_student_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Student", modifier = Modifier.size(20.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sleek Header & Scrollable Options Strip (Sticky top for fast access)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Students (${filteredRecords.size})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (selectedStatusFilter != "All" || selectedClassFilter != "All") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Filtered",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(16.dp),
                            tint = if (isSearchVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = onManageBatchesClick,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Batches", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Animated Collapsible Search Bar
            AnimatedVisibility(visible = isSearchVisible) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search name, roll no, or phone...", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(14.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(13.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .height(38.dp)
                        .testTag("student_search_bar"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            // Horizontal Scrollable Options (Status + Class Filters)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(statusFilters) { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { onStatusFilterChange(status) },
                        label = { Text(status, fontSize = 10.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }

                if (availableClasses.size > 2) {
                    items(availableClasses) { cls ->
                        FilterChip(
                            selected = selectedClassFilter == cls,
                            onClick = { onClassFilterChange(cls) },
                            label = { Text(if (cls == "All") "All Classes" else cls, fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            // Student List with Maximum Viewport Area
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

            // Student List or Empty State
            if (filteredRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No students found matching filters",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(filteredRecords, key = { it.student.id }) { record ->
                    StudentCardItem(
                        record = record,
                        currencySymbol = currencySymbol,
                        onCardClick = { onStudentClick(record.student) },
                        onEditClick = { onEditStudentClick(record.student) },
                        onDeleteClick = { onDeleteStudentClick(record.student) },
                        onCollectFeeClick = { onCollectFeeClick(record.student) },
                        onViewReceiptClick = {
                            if (record.payment != null) {
                                onViewReceiptClick(record.payment)
                            }
                        },
                        onAiReminderClick = { onAiReminderClick(record) },
                        onSendEnrollmentClick = { onSendEnrollmentClick(record.student) },
                        onWithdrawStudentClick = { onWithdrawStudentClick(record.student) }
                    )
                }
            }
        }
    }
}
}

@Composable
private fun StudentCardItem(
    record: StudentMonthlyFeeRecord,
    currencySymbol: String,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCollectFeeClick: () -> Unit,
    onViewReceiptClick: () -> Unit,
    onAiReminderClick: () -> Unit,
    onSendEnrollmentClick: () -> Unit = {},
    onWithdrawStudentClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Student?") },
            text = { Text("Are you sure you want to remove '${record.student.name}'? All history will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(
                    name = record.student.name,
                    photoUri = record.student.photoUri,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        if (record.student.rollNo.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${record.student.rollNo})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "${record.student.gradeClass} • ${record.student.batch}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Fee: $currencySymbol${record.student.monthlyFee.toInt()}/mo • Due ${record.student.dueDay}th",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Welcome Notice (WhatsApp)", fontSize = 12.sp) },
                                onClick = {
                                    menuExpanded = false
                                    onSendEnrollmentClick()
                                },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Exit / Withdraw Student", fontSize = 12.sp) },
                                onClick = {
                                    menuExpanded = false
                                    onWithdrawStudentClick()
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Student", fontSize = 12.sp) },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Student", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteConfirm = true
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                    MonthStatusBadge(status = record.status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row (Compact & Fast)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record.status == MonthFeeStatus.PAID) {
                    Button(
                        onClick = onViewReceiptClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onCollectFeeClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .testTag("student_card_collect_fee"),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Collect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val phone = record.student.parentPhone.ifEmpty { record.student.phone }
                            val msg = WhatsAppHelper.buildOverdueFeeReminder(
                                student = record.student,
                                monthYear = record.monthYear,
                                amountDue = record.amountDue,
                                dueDate = "${record.student.dueDay}th",
                                daysOverdue = record.daysOverdue
                            )
                            WhatsAppHelper.sendWhatsAppMessage(context, phone, msg)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onAiReminderClick,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(32.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("AI", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
