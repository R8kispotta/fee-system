package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.Student
import com.example.data.model.InstituteSettings
import com.example.ui.theme.WhatsAppColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalNotificationDialog(
    student: Student,
    settings: InstituteSettings,
    pendingDueAmount: Double,
    onDismiss: () -> Unit,
    onConfirmWithdrawal: (withdrawalDate: String, reason: String, sendWhatsApp: Boolean, targetPhone: String, message: String) -> Unit
) {
    val context = LocalContext.current
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var withdrawalDate by remember { mutableStateOf(todayStr) }
    var reason by remember { mutableStateOf("Course Completed Successfully 🎓") }
    var clearanceStatus by remember {
        mutableStateOf(
            if (pendingDueAmount <= 0) "All Dues Cleared (₹0 Pending) ✅"
            else "Pending Dues: ${settings.currencySymbol}${pendingDueAmount.toInt()}"
        )
    }

    var selectedRecipient by remember {
        mutableStateOf(if (student.parentPhone.isNotEmpty()) "Parent" else "Student")
    }

    val reasons = listOf(
        "Course Completed Successfully 🎓",
        "Exam Preparation Completed 📚",
        "Relocation / Shifted City 🏡",
        "Schedule / Timing Conflict ⏰",
        "Personal / Health Reasons 🩺",
        "Other"
    )
    var reasonDropdownExpanded by remember { mutableStateOf(false) }

    val clearanceOptions = listOf(
        "All Dues Cleared (₹0 Pending) ✅",
        "Final Settlement Completed 💳",
        "Special Concession / Exempted 🤝",
        "Remaining Balance: ${settings.currencySymbol}${pendingDueAmount.toInt()} ⚠️"
    )
    var clearanceDropdownExpanded by remember { mutableStateOf(false) }

    var editableMessage by remember {
        mutableStateOf(
            WhatsAppHelper.buildWithdrawalMessage(
                student = student,
                withdrawalDate = withdrawalDate,
                reason = reason,
                clearanceStatus = clearanceStatus,
                settings = settings
            )
        )
    }

    // Refresh message when date/reason/clearance updates
    LaunchedEffect(withdrawalDate, reason, clearanceStatus) {
        editableMessage = WhatsAppHelper.buildWithdrawalMessage(
            student = student,
            withdrawalDate = withdrawalDate,
            reason = reason,
            clearanceStatus = clearanceStatus,
            settings = settings
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Output,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Student Exit / Withdrawal",
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
                // Student info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
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

                Spacer(modifier = Modifier.height(12.dp))

                // Effective Date
                OutlinedTextField(
                    value = withdrawalDate,
                    onValueChange = { withdrawalDate = it },
                    label = { Text("Effective Withdrawal / Exit Date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Exit Reason Dropdown
                ExposedDropdownMenuBox(
                    expanded = reasonDropdownExpanded,
                    onExpandedChange = { reasonDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason / Exit Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = reasonDropdownExpanded,
                        onDismissRequest = { reasonDropdownExpanded = false }
                    ) {
                        reasons.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    reason = r
                                    reasonDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clearance Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = clearanceDropdownExpanded,
                    onExpandedChange = { clearanceDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = clearanceStatus,
                        onValueChange = { clearanceStatus = it },
                        label = { Text("Fee Clearance Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clearanceDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = clearanceDropdownExpanded,
                        onDismissRequest = { clearanceDropdownExpanded = false }
                    ) {
                        clearanceOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    clearanceStatus = opt
                                    clearanceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Recipient selector
                Text(
                    text = "Send WhatsApp Notice to:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRecipient == "Student",
                        onClick = { selectedRecipient = "Student" },
                        label = { Text("Student: ${student.phone}", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )

                    if (student.parentPhone.isNotEmpty()) {
                        FilterChip(
                            selected = selectedRecipient == "Parent",
                            onClick = { selectedRecipient = "Parent" },
                            label = { Text("Parent: ${student.parentPhone}", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Message Draft preview
                OutlinedTextField(
                    value = editableMessage,
                    onValueChange = { editableMessage = it },
                    label = { Text("Formal Withdrawal Letter (WhatsApp)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("withdrawal_message_preview"),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetPhone = if (selectedRecipient == "Parent" && student.parentPhone.isNotEmpty()) {
                        student.parentPhone
                    } else {
                        student.phone
                    }
                    onConfirmWithdrawal(withdrawalDate, reason, true, targetPhone, editableMessage)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppColor),
                modifier = Modifier.testTag("confirm_withdrawal_whatsapp_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirm & Send WhatsApp")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val targetPhone = if (selectedRecipient == "Parent" && student.parentPhone.isNotEmpty()) {
                        student.parentPhone
                    } else {
                        student.phone
                    }
                    onConfirmWithdrawal(withdrawalDate, reason, false, targetPhone, editableMessage)
                }
            ) {
                Text("Withdraw Only (No WhatsApp)")
            }
        }
    )
}
