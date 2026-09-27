package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Text
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
import com.example.ui.components.PrintReceiptHelper
import com.example.ui.components.StudentAvatar
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
    modifier: Modifier = Modifier
) {
    val statusFilters = listOf("All", "Pending", "Overdue", "Paid", "Withdrawn")

    // Extract available classes dynamically
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by name, roll no, or phone...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search_bar"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusFilters) { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { onStatusFilterChange(status) },
                        label = { Text(status) }
                    )
                }
            }

            // Class Filter Chips if multiple
            if (availableClasses.size > 2) {
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableClasses) { cls ->
                        FilterChip(
                            selected = selectedClassFilter == cls,
                            onClick = { onClassFilterChange(cls) },
                            label = { Text(cls, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Count Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredRecords.size} Students found",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Student List
            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No students match your criteria",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onAddStudentClick) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Student")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(
                    name = record.student.name,
                    photoUri = record.student.photoUri,
                    size = 50.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (record.student.rollNo.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${record.student.rollNo})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "${record.student.gradeClass} • ${record.student.batch}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Fee: $currencySymbol${record.student.monthlyFee.toInt()}/mo (Due ${record.student.dueDay}th)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Welcome Notice (WhatsApp)") },
                                onClick = {
                                    menuExpanded = false
                                    onSendEnrollmentClick()
                                },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Exit / Withdraw Student") },
                                onClick = {
                                    menuExpanded = false
                                    onWithdrawStudentClick()
                                },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Student") },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Student", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteConfirm = true
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }

                    MonthStatusBadge(status = record.status)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record.status == MonthFeeStatus.PAID) {
                    Button(
                        onClick = onViewReceiptClick,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Receipt", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onCollectFeeClick,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Collect Fee", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onAiReminderClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Reminder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Remind", fontSize = 12.sp)
                    }
                }

                // Direct WhatsApp Button
                Button(
                    onClick = {
                        val targetPhone = record.student.parentPhone.ifEmpty { record.student.phone }
                        val quickMsg = if (record.status == MonthFeeStatus.PAID) {
                            "Hello, fee for ${record.student.name} for ${record.monthYear} is paid. Thank you!"
                        } else {
                            "Hello, gentle reminder: Tuition fee for ${record.student.name} for ${record.monthYear} (₹${record.amountDue.toInt()}) is due. Kindly clear the payment. Thank you!"
                        }
                        PrintReceiptHelper.sendWhatsAppMessage(context, targetPhone, quickMsg)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("WhatsApp", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
