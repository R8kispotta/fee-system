package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FeePayment
import com.example.data.local.Student
import com.example.data.model.MonthFeeStatus
import com.example.ui.components.MonthStatusBadge
import com.example.ui.components.PrintReceiptHelper
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.WhatsAppColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    student: Student,
    payments: List<FeePayment>,
    currencySymbol: String,
    onBack: () -> Unit,
    onEditClick: (Student) -> Unit,
    onCollectFeeClick: (Student) -> Unit,
    onViewReceiptClick: (FeePayment) -> Unit,
    onGenerateAiReport: (Student) -> Unit,
    onSendEnrollmentClick: (Student) -> Unit = {},
    onWithdrawStudentClick: (Student) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    // Generate 12 months matrix for current year
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonthZeroBased = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    data class MonthLedgerItem(
        val monthName: String,
        val monthYear: String,
        val monthKey: Int,
        val payment: FeePayment?,
        val status: MonthFeeStatus
    )

    val ledger = remember(student, payments) {
        monthNames.mapIndexed { idx, mName ->
            val monthYear = "$mName $currentYear"
            val monthKey = currentYear * 100 + (idx + 1)
            val payment = payments.find { it.studentId == student.id && it.monthKey == monthKey }

            val status = when {
                payment != null && payment.amountPaid >= student.monthlyFee -> MonthFeeStatus.PAID
                payment != null && payment.amountPaid > 0 -> MonthFeeStatus.PARTIALLY_PAID
                idx < currentMonthZeroBased -> MonthFeeStatus.OVERDUE
                idx == currentMonthZeroBased && currentDay > student.dueDay -> MonthFeeStatus.OVERDUE
                idx == currentMonthZeroBased -> MonthFeeStatus.PENDING
                else -> MonthFeeStatus.PENDING
            }

            MonthLedgerItem(
                monthName = mName,
                monthYear = monthYear,
                monthKey = monthKey,
                payment = payment,
                status = status
            )
        }
    }

    val totalPaidSoFar = remember(payments, student) {
        payments.filter { it.studentId == student.id }.sumOf { it.amountPaid }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(student.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(student) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StudentAvatar(name = student.name, photoUri = student.photoUri, size = 68.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "${student.gradeClass} • ${student.batch}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (student.rollNo.isNotEmpty()) {
                                    Text(
                                        text = "Roll ID: ${student.rollNo}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Details grid
                        DetailRow("Contact Phone", student.phone)
                        if (student.parentPhone.isNotEmpty()) {
                            DetailRow("Parent Phone", student.parentPhone)
                        }
                        DetailRow("Monthly Fee", "$currencySymbol${student.monthlyFee.toInt()} (Due on ${student.dueDay}th)")
                        DetailRow("Total Paid (Lifetime)", "$currencySymbol${totalPaidSoFar.toInt()}")
                        DetailRow("Enrollment Status", student.enrollmentStatus)
                        if (student.enrollmentStatus == "WITHDRAWN" && student.withdrawalDate.isNotEmpty()) {
                            DetailRow("Withdrawal Date", student.withdrawalDate)
                            if (student.withdrawalReason.isNotEmpty()) {
                                DetailRow("Withdrawal Reason", student.withdrawalReason)
                            }
                        }
                        if (student.admissionDate.isNotEmpty()) {
                            DetailRow("Admission Date", student.admissionDate)
                        }
                        if (student.notes.isNotEmpty()) {
                            DetailRow("Remarks", student.notes)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Call, WhatsApp, Collect, AI Report
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onCollectFeeClick(student) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("student_detail_collect_fee"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Collect Fee")
                            }

                            Button(
                                onClick = {
                                    val phone = student.parentPhone.ifEmpty { student.phone }
                                    val text = "Hello from coaching academy regarding ${student.name}'s tuition."
                                    com.example.ui.components.WhatsAppHelper.sendWhatsAppMessage(context, phone, text)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("WhatsApp", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // WhatsApp Lifecycle Actions: Welcome Letter & Exit / Withdrawal Notice
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSendEnrollmentClick(student) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Welcome Notice", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onWithdrawStudentClick(student) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (student.enrollmentStatus == "WITHDRAWN") "Exit Details" else "Exit / Withdraw",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onGenerateAiReport(student) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Progress & Fee Report")
                        }
                    }
                }
            }

            // Monthly Fee Ledger Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fee History ($currentYear)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "12 Months Record",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 12 Months Ledger
            items(ledger) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.monthName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (item.payment != null) {
                                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                val pDateStr = dateFormat.format(Date(item.payment.paymentDate))
                                Text(
                                    text = "Paid on $pDateStr (${item.payment.paymentMode})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Receipt: ${item.payment.receiptNo}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Due on ${student.dueDay}th • $currencySymbol${student.monthlyFee.toInt()}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MonthStatusBadge(status = item.status)
                            Spacer(modifier = Modifier.width(8.dp))

                            if (item.payment != null) {
                                IconButton(onClick = { onViewReceiptClick(item.payment) }) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = "View Receipt",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
