package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.StudentMonthlyFeeRecord
import com.example.ui.components.AddEditStudentDialog
import com.example.ui.components.AdminLockDialog
import com.example.ui.components.AiInsightsDialog
import com.example.ui.components.AiReminderDialog
import com.example.ui.components.EnrollmentNotificationDialog
import com.example.ui.components.FeePaymentDialog
import com.example.ui.components.ReceiptViewDialog
import com.example.ui.components.WithdrawalNotificationDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.screens.WhatsAppHubScreen
import com.example.ui.theme.FeeTrackTheme

enum class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    STUDENTS("Students", Icons.Default.People, "tab_students"),
    WHATSAPP("Reminders", Icons.Default.NotificationsActive, "tab_whatsapp"),
    LEDGER("Receipts", Icons.AutoMirrored.Filled.ReceiptLong, "tab_ledger"),
    ATTENDANCE("Attendance", Icons.Default.DateRange, "tab_attendance"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings")
}

@Composable
fun MainScreen(viewModel: FeeViewModel = viewModel()) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isDarkTheme = when (settings.themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    FeeTrackTheme(darkTheme = isDarkTheme) {
        val isPinLocked by viewModel.isPinLocked.collectAsStateWithLifecycle()

        if (isPinLocked) {
            AdminLockDialog(
                instituteName = settings.name,
                onUnlock = { pin -> viewModel.unlockWithPin(pin) }
            )
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

        LaunchedEffect(snackbarMsg) {
            if (snackbarMsg != null) {
                snackbarHostState.showSnackbar(snackbarMsg!!)
                viewModel.clearSnackbar()
            }
        }

        var currentTab by remember { mutableStateOf(NavTab.DASHBOARD) }

        val students by viewModel.students.collectAsStateWithLifecycle()
        val payments by viewModel.payments.collectAsStateWithLifecycle()
        val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
        val monthlyRecords by viewModel.monthlyStudentRecords.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        val statusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
        val classFilter by viewModel.selectedClassFilter.collectAsStateWithLifecycle()

        val attendanceList by viewModel.attendanceList.collectAsStateWithLifecycle()
        val selectedAttendanceDate by viewModel.selectedAttendanceDate.collectAsStateWithLifecycle()
        val selectedAttendanceBatch by viewModel.selectedAttendanceBatch.collectAsStateWithLifecycle()

        val selectedDetailStudent by viewModel.selectedStudentDetail.collectAsStateWithLifecycle()
        val isAddEditOpen by viewModel.isAddEditStudentOpen.collectAsStateWithLifecycle()
        val studentToEdit by viewModel.studentToEdit.collectAsStateWithLifecycle()

        val isPaymentOpen by viewModel.isPaymentDialogOpen.collectAsStateWithLifecycle()
        val studentForPayment by viewModel.studentForPayment.collectAsStateWithLifecycle()

        val isReceiptOpen by viewModel.isReceiptDialogOpen.collectAsStateWithLifecycle()
        val selectedReceipt by viewModel.selectedReceipt.collectAsStateWithLifecycle()

        // Enrollment & Withdrawal Lifecycle Dialog States (First Entry & Last Entry)
        val isEnrollmentOpen by viewModel.isEnrollmentDialogOpen.collectAsStateWithLifecycle()
        val newlyEnrolledStudent by viewModel.newlyEnrolledStudent.collectAsStateWithLifecycle()

        val isWithdrawalOpen by viewModel.isWithdrawalDialogOpen.collectAsStateWithLifecycle()
        val studentForWithdrawal by viewModel.studentForWithdrawal.collectAsStateWithLifecycle()

        // AI Reminder Dialog State
        var aiReminderRecord by remember { mutableStateOf<StudentMonthlyFeeRecord?>(null) }
        val aiReminderText by viewModel.aiReminderText.collectAsStateWithLifecycle()
        val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()

        // AI Insights Dialog State
        var isAiInsightsOpen by remember { mutableStateOf(false) }
        val aiInsightsText by viewModel.aiInsightsText.collectAsStateWithLifecycle()

        // Add/Edit Student Dialog
        if (isAddEditOpen) {
            AddEditStudentDialog(
                studentToEdit = studentToEdit,
                onDismiss = { viewModel.isAddEditStudentOpen.value = false },
                onSave = { student -> viewModel.saveStudent(student) }
            )
        }

        // Enrollment Welcome Notification Dialog (Triggered on First Entry or manually)
        if (isEnrollmentOpen && newlyEnrolledStudent != null) {
            EnrollmentNotificationDialog(
                student = newlyEnrolledStudent!!,
                settings = settings,
                onDismiss = {
                    viewModel.isEnrollmentDialogOpen.value = false
                    viewModel.newlyEnrolledStudent.value = null
                }
            )
        }

        // Withdrawal Exit Notification Dialog (Triggered on Last Entry / Exit)
        if (isWithdrawalOpen && studentForWithdrawal != null) {
            val pendingDue = monthlyRecords.find { it.student.id == studentForWithdrawal!!.id }?.amountDue ?: 0.0
            WithdrawalNotificationDialog(
                student = studentForWithdrawal!!,
                settings = settings,
                pendingDueAmount = pendingDue,
                onDismiss = {
                    viewModel.isWithdrawalDialogOpen.value = false
                    viewModel.studentForWithdrawal.value = null
                },
                onConfirmWithdrawal = { withdrawalDate, reason, sendWhatsApp, targetPhone, message ->
                    viewModel.withdrawStudent(
                        student = studentForWithdrawal!!,
                        withdrawalDate = withdrawalDate,
                        reason = reason,
                        sendWhatsApp = sendWhatsApp,
                        targetPhone = targetPhone,
                        message = message,
                        context = context
                    )
                }
            )
        }

        // Fee Payment Dialog
        if (isPaymentOpen && studentForPayment != null) {
            FeePaymentDialog(
                student = studentForPayment!!,
                currentMonthYear = summary.selectedMonthYear,
                currentMonthKey = summary.selectedMonthKey,
                currencySymbol = settings.currencySymbol,
                onDismiss = {
                    viewModel.isPaymentDialogOpen.value = false
                    viewModel.studentForPayment.value = null
                },
                onConfirm = { amount, discount, monthYear, monthKey, mode, txnId, remarks ->
                    viewModel.collectFee(
                        student = studentForPayment!!,
                        amount = amount,
                        discount = discount,
                        monthYear = monthYear,
                        monthKey = monthKey,
                        paymentMode = mode,
                        transactionId = txnId,
                        remarks = remarks,
                        onReceiptCreated = { /* handled in VM */ }
                    )
                }
            )
        }

        // Receipt View Dialog
        if (isReceiptOpen && selectedReceipt != null) {
            val student = students.find { it.id == selectedReceipt!!.studentId }
            ReceiptViewDialog(
                payment = selectedReceipt!!,
                student = student,
                settings = settings,
                onDismiss = {
                    viewModel.isReceiptDialogOpen.value = false
                    viewModel.selectedReceipt.value = null
                },
                onDelete = { payment ->
                    viewModel.deletePaymentRecord(payment)
                }
            )
        }

        // AI Reminder Dialog
        if (aiReminderRecord != null) {
            AiReminderDialog(
                record = aiReminderRecord!!,
                initialMessage = aiReminderText,
                isLoading = isAiGenerating,
                onDismiss = { aiReminderRecord = null },
                onGenerate = { tone ->
                    viewModel.generateAiReminder(aiReminderRecord!!, tone)
                }
            )
        }

        // AI Insights Dialog
        if (isAiInsightsOpen) {
            AiInsightsDialog(
                insightsText = aiInsightsText,
                isLoading = isAiGenerating,
                onDismiss = { isAiInsightsOpen = false },
                onRefresh = { viewModel.generateInstituteAiInsights() }
            )
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (selectedDetailStudent == null) {
                    NavigationBar {
                        NavTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedDetailStudent != null) {
                    StudentDetailScreen(
                        student = selectedDetailStudent!!,
                        payments = payments,
                        currencySymbol = settings.currencySymbol,
                        onBack = { viewModel.selectedStudentDetail.value = null },
                        onEditClick = { student ->
                            viewModel.studentToEdit.value = student
                            viewModel.isAddEditStudentOpen.value = true
                        },
                        onCollectFeeClick = { student ->
                            viewModel.studentForPayment.value = student
                            viewModel.isPaymentDialogOpen.value = true
                        },
                        onViewReceiptClick = { payment ->
                            viewModel.selectedReceipt.value = payment
                            viewModel.isReceiptDialogOpen.value = true
                        },
                        onGenerateAiReport = { student ->
                            viewModel.generateInstituteAiInsights()
                            isAiInsightsOpen = true
                        },
                        onSendEnrollmentClick = { student ->
                            viewModel.newlyEnrolledStudent.value = student
                            viewModel.isEnrollmentDialogOpen.value = true
                        },
                        onWithdrawStudentClick = { student ->
                            viewModel.studentForWithdrawal.value = student
                            viewModel.isWithdrawalDialogOpen.value = true
                        }
                    )
                } else {
                    when (currentTab) {
                        NavTab.DASHBOARD -> {
                            DashboardScreen(
                                summary = summary,
                                settings = settings,
                                recentPayments = payments,
                                onMonthChange = { offset -> viewModel.changeMonth(offset) },
                                onAddStudentClick = {
                                    viewModel.studentToEdit.value = null
                                    viewModel.isAddEditStudentOpen.value = true
                                },
                                onCollectFeeClick = {
                                    if (students.isNotEmpty()) {
                                        viewModel.studentForPayment.value = students.first()
                                        viewModel.isPaymentDialogOpen.value = true
                                    } else {
                                        viewModel.studentToEdit.value = null
                                        viewModel.isAddEditStudentOpen.value = true
                                    }
                                },
                                onTakeAttendanceClick = { currentTab = NavTab.ATTENDANCE },
                                onAiInsightsClick = {
                                    viewModel.generateInstituteAiInsights()
                                    isAiInsightsOpen = true
                                },
                                onPaymentClick = { payment ->
                                    viewModel.selectedReceipt.value = payment
                                    viewModel.isReceiptDialogOpen.value = true
                                },
                                onViewStudentsClick = { currentTab = NavTab.STUDENTS },
                                onWhatsAppHubClick = { currentTab = NavTab.WHATSAPP }
                            )
                        }

                        NavTab.STUDENTS -> {
                            StudentsScreen(
                                records = monthlyRecords,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.searchQuery.value = it },
                                selectedStatusFilter = statusFilter,
                                onStatusFilterChange = { viewModel.selectedStatusFilter.value = it },
                                selectedClassFilter = classFilter,
                                onClassFilterChange = { viewModel.selectedClassFilter.value = it },
                                currencySymbol = settings.currencySymbol,
                                onAddStudentClick = {
                                    viewModel.studentToEdit.value = null
                                    viewModel.isAddEditStudentOpen.value = true
                                },
                                onStudentClick = { student ->
                                    viewModel.selectedStudentDetail.value = student
                                },
                                onEditStudentClick = { student ->
                                    viewModel.studentToEdit.value = student
                                    viewModel.isAddEditStudentOpen.value = true
                                },
                                onDeleteStudentClick = { student ->
                                    viewModel.deleteStudent(student)
                                },
                                onCollectFeeClick = { student ->
                                    viewModel.studentForPayment.value = student
                                    viewModel.isPaymentDialogOpen.value = true
                                },
                                onViewReceiptClick = { payment ->
                                    viewModel.selectedReceipt.value = payment
                                    viewModel.isReceiptDialogOpen.value = true
                                },
                                onAiReminderClick = { record ->
                                    aiReminderRecord = record
                                    viewModel.generateAiReminder(record)
                                },
                                onSendEnrollmentClick = { student ->
                                    viewModel.newlyEnrolledStudent.value = student
                                    viewModel.isEnrollmentDialogOpen.value = true
                                },
                                onWithdrawStudentClick = { student ->
                                    viewModel.studentForWithdrawal.value = student
                                    viewModel.isWithdrawalDialogOpen.value = true
                                }
                            )
                        }

                        NavTab.WHATSAPP -> {
                            WhatsAppHubScreen(
                                records = monthlyRecords,
                                settings = settings,
                                onSendAiCustomReminder = { record ->
                                    aiReminderRecord = record
                                    viewModel.generateAiReminder(record)
                                },
                                onEnrollmentNotification = { student ->
                                    viewModel.newlyEnrolledStudent.value = student
                                    viewModel.isEnrollmentDialogOpen.value = true
                                },
                                onWithdrawalNotification = { student ->
                                    viewModel.studentForWithdrawal.value = student
                                    viewModel.isWithdrawalDialogOpen.value = true
                                }
                            )
                        }

                        NavTab.LEDGER -> {
                            LedgerScreen(
                                payments = payments,
                                students = students,
                                settings = settings,
                                onReceiptClick = { payment ->
                                    viewModel.selectedReceipt.value = payment
                                    viewModel.isReceiptDialogOpen.value = true
                                },
                                onCollectFeeClick = {
                                    if (students.isNotEmpty()) {
                                        viewModel.studentForPayment.value = students.first()
                                        viewModel.isPaymentDialogOpen.value = true
                                    } else {
                                        viewModel.studentToEdit.value = null
                                        viewModel.isAddEditStudentOpen.value = true
                                    }
                                }
                            )
                        }

                        NavTab.ATTENDANCE -> {
                            AttendanceScreen(
                                students = students,
                                attendanceList = attendanceList,
                                selectedDate = selectedAttendanceDate,
                                onDateChange = { date -> viewModel.loadAttendanceForDate(date) },
                                selectedBatch = selectedAttendanceBatch,
                                onBatchChange = { viewModel.selectedAttendanceBatch.value = it },
                                onMarkStatus = { id, status, batch ->
                                    viewModel.markStudentAttendance(id, status, batch)
                                },
                                onMarkAllPresent = { batch ->
                                    viewModel.markAllPresent(batch)
                                }
                            )
                        }

                        NavTab.SETTINGS -> {
                            SettingsScreen(
                                settings = settings,
                                onSaveSettings = { updated -> viewModel.saveInstituteSettings(updated) },
                                onExportJson = { onReady -> viewModel.exportBackupJson(onReady) },
                                onRestoreJson = { json -> viewModel.restoreBackupJson(json) },
                                onExportStudentsCsv = { onReady -> viewModel.exportStudentsCsv(onReady) },
                                onExportPaymentsCsv = { onReady -> viewModel.exportPaymentsCsv(onReady) },
                                onRunAiAnalytics = {
                                    viewModel.generateInstituteAiInsights()
                                    isAiInsightsOpen = true
                                },
                                onLockDashboard = { viewModel.lockAdmin() }
                            )
                        }
                    }
                }
            }
        }
    }
}
