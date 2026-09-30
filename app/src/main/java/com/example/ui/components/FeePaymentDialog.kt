package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Student
import com.example.data.model.InstituteSettings

@Composable
fun FeePaymentDialog(
    student: Student,
    currentMonthYear: String,
    currentMonthKey: Int,
    currencySymbol: String,
    instituteSettings: InstituteSettings = InstituteSettings(),
    onDismiss: () -> Unit,
    onConfirm: (
        amount: Double,
        discount: Double,
        monthYear: String,
        monthKey: Int,
        mode: String,
        txnId: String,
        remarks: String,
        monthsCovered: Int,
        coveragePeriod: String
    ) -> Unit
) {
    var monthsCovered by remember { mutableStateOf(1) }
    var coveragePeriod by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf(student.monthlyFee.toInt().toString()) }
    var discountText by remember { mutableStateOf("0") }
    var paymentMode by remember { mutableStateOf("UPI") } // "UPI", "Cash", "Bank Transfer", "Cheque"
    var transactionId by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    val paymentModes = listOf("UPI", "Cash", "Bank Transfer", "Cheque")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Collect Fee",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Student info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StudentAvatar(name = student.name, photoUri = student.photoUri, size = 44.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${student.gradeClass} • ${student.batch}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fee Month Display
                Text(
                    text = "Payment For: $currentMonthYear",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Multi-Month / Advance Period selector
                Text(
                    text = "Billing Duration / Advance Payment",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val durationOptions = listOf(
                        1 to "1 Mo",
                        2 to "2 Mo",
                        3 to "3 Mo (Quarterly)",
                        6 to "6 Mo (Half-Yr)"
                    )
                    durationOptions.forEach { (count, label) ->
                        FilterChip(
                            selected = monthsCovered == count,
                            onClick = {
                                monthsCovered = count
                                coveragePeriod = if (count > 1) "$count Months Advance" else ""
                                val disc = discountText.toDoubleOrNull() ?: 0.0
                                val totalFee = (student.monthlyFee * count - disc).coerceAtLeast(0.0)
                                amountText = totalFee.toInt().toString()
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = it.toDoubleOrNull() == null
                    },
                    label = { Text("Amount Received ($currencySymbol) *") },
                    isError = amountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Discount / Waiver
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Discount / Waiver ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Mode Selection
                Text(
                    text = "Payment Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentModes.take(2).forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentModes.drop(2).forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transaction Ref (if UPI or Bank Transfer)
                if (paymentMode != "Cash") {
                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("UTR / Ref / Transaction ID") },
                        placeholder = { Text("e.g. UPI-92819482") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks (Optional)") },
                    placeholder = { Text("e.g. Paid in full by father") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (paymentMode == "UPI" && instituteSettings.upiId.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val enteredAmount = amountText.toDoubleOrNull() ?: student.monthlyFee
                    UpiQrCodeCard(
                        upiId = instituteSettings.upiId,
                        payeeName = instituteSettings.name,
                        amount = enteredAmount,
                        note = "Fee - ${student.name} (${student.rollNo})",
                        currencySymbol = currencySymbol
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        val discount = discountText.toDoubleOrNull() ?: 0.0
                        onConfirm(
                            amount,
                            discount,
                            currentMonthYear,
                            currentMonthKey,
                            paymentMode,
                            transactionId.trim(),
                            remarks.trim(),
                            monthsCovered,
                            coveragePeriod.trim()
                        )
                    } else {
                        amountError = true
                    }
                },
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Confirm & Generate Receipt")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
