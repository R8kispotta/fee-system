package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiFeeService
import com.example.data.local.AppDatabase
import com.example.data.local.Attendance
import com.example.data.local.BatchItem
import com.example.data.local.FeePayment
import com.example.data.local.Student
import com.example.data.model.DashboardSummary
import com.example.data.model.InstituteSettings
import com.example.data.model.MonthFeeStatus
import com.example.data.model.StudentMonthlyFeeRecord
import com.example.data.repository.FeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FeeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = FeeRepository(database.appDao(), application)
    private val geminiService = GeminiFeeService()

    val students: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<FeePayment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<BatchItem>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _settings = MutableStateFlow(repository.getSettings())
    val settings: StateFlow<InstituteSettings> = _settings.asStateFlow()

    // PIN lock state
    private val _isPinLocked = MutableStateFlow(false)
    val isPinLocked: StateFlow<Boolean> = _isPinLocked.asStateFlow()

    // Selected Month for filtering fee records
    private val calendar = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val monthKeyFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())

    private val _selectedMonthYear = MutableStateFlow(monthFormat.format(calendar.time))
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    private val _selectedMonthKey = MutableStateFlow(monthKeyFormat.format(calendar.time).toInt())
    val selectedMonthKey: StateFlow<Int> = _selectedMonthKey.asStateFlow()

    // Search and filters
    val searchQuery = MutableStateFlow("")
    val selectedClassFilter = MutableStateFlow("All")
    val selectedStatusFilter = MutableStateFlow("All") // "All", "Paid", "Pending", "Overdue"

    // Attendance State
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val selectedAttendanceDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedAttendanceBatch = MutableStateFlow("All")

    private val _attendanceList = MutableStateFlow<List<Attendance>>(emptyList())
    val attendanceList: StateFlow<List<Attendance>> = _attendanceList.asStateFlow()

    // Dialog / Sheet states
    val studentToEdit = MutableStateFlow<Student?>(null)
    val isAddEditStudentOpen = MutableStateFlow(false)

    val studentForPayment = MutableStateFlow<Student?>(null)
    val isPaymentDialogOpen = MutableStateFlow(false)

    val selectedReceipt = MutableStateFlow<FeePayment?>(null)
    val isReceiptDialogOpen = MutableStateFlow(false)

    val selectedStudentDetail = MutableStateFlow<Student?>(null)

    // Enrollment & Withdrawal Dialog states (First Entry & Last Entry)
    val isEnrollmentDialogOpen = MutableStateFlow(false)
    val newlyEnrolledStudent = MutableStateFlow<Student?>(null)

    val isWithdrawalDialogOpen = MutableStateFlow(false)
    val studentForWithdrawal = MutableStateFlow<Student?>(null)

    // AI States
    val aiReminderText = MutableStateFlow<String?>(null)
    val isAiGenerating = MutableStateFlow(false)
    val aiInsightsText = MutableStateFlow<String?>(null)

    // User Feedback Snackbars
    val snackbarMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            // Check if PIN lock is active on start
            if (_settings.value.isPinProtectionEnabled && _settings.value.adminPin.isNotEmpty()) {
                _isPinLocked.value = true
            }
            loadAttendanceForDate(selectedAttendanceDate.value)
        }
    }

    // Monthly Fee Records Computed Flow
    val monthlyStudentRecords: StateFlow<List<StudentMonthlyFeeRecord>> = combine(
        students,
        payments,
        _selectedMonthKey,
        _selectedMonthYear
    ) { studentList, paymentList, monthKey, monthYear ->
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val currentMonthKey = monthKeyFormat.format(Date()).toInt()

        studentList.map { student ->
            val monthPayment = paymentList.find { it.studentId == student.id && it.monthKey == monthKey }
            val amountPaid = monthPayment?.amountPaid ?: 0.0
            val amountDue = (student.monthlyFee - (monthPayment?.discount ?: 0.0) - amountPaid).coerceAtLeast(0.0)

            val isDuePassed = (monthKey < currentMonthKey) || (monthKey == currentMonthKey && currentDay > student.dueDay)
            val isOverdue = amountPaid < student.monthlyFee && isDuePassed
            val daysOverdue = if (isOverdue) {
                if (monthKey == currentMonthKey) currentDay - student.dueDay else 30
            } else 0

            val status = when {
                amountPaid >= student.monthlyFee -> MonthFeeStatus.PAID
                amountPaid > 0 -> MonthFeeStatus.PARTIALLY_PAID
                isOverdue -> MonthFeeStatus.OVERDUE
                else -> MonthFeeStatus.PENDING
            }

            StudentMonthlyFeeRecord(
                student = student,
                monthYear = monthYear,
                monthKey = monthKey,
                status = status,
                amountDue = amountDue,
                amountPaid = amountPaid,
                payment = monthPayment,
                isOverdue = isOverdue,
                daysOverdue = daysOverdue
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Financial Summary
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        students,
        monthlyStudentRecords,
        _selectedMonthYear,
        _selectedMonthKey
    ) { studentList, records, monthYear, monthKey ->
        val totalExpected = studentList.sumOf { it.monthlyFee }
        val totalCollected = records.sumOf { it.amountPaid }
        val totalPending = records.sumOf { it.amountDue }
        val overdueRecords = records.filter { it.isOverdue }
        val overdueCount = overdueRecords.size
        val overdueAmount = overdueRecords.sumOf { it.amountDue }
        val percentage = if (totalExpected > 0) ((totalCollected / totalExpected) * 100).toFloat() else 0f

        DashboardSummary(
            totalStudents = studentList.size,
            collectedThisMonth = totalCollected,
            pendingThisMonth = totalPending,
            totalExpectedThisMonth = totalExpected,
            overdueCount = overdueCount,
            overdueAmount = overdueAmount,
            collectionPercentage = percentage,
            selectedMonthYear = monthYear,
            selectedMonthKey = monthKey
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Month Navigation
    fun changeMonth(offsetMonths: Int) {
        val cal = Calendar.getInstance()
        val currentYear = _selectedMonthKey.value / 100
        val currentMonth = (_selectedMonthKey.value % 100) - 1
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.add(Calendar.MONTH, offsetMonths)

        _selectedMonthYear.value = monthFormat.format(cal.time)
        _selectedMonthKey.value = monthKeyFormat.format(cal.time).toInt()
    }

    fun setSpecificMonth(year: Int, monthIndexZeroBased: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndexZeroBased)
        _selectedMonthYear.value = monthFormat.format(cal.time)
        _selectedMonthKey.value = monthKeyFormat.format(cal.time).toInt()
    }

    // Student CRUD
    fun saveStudent(student: Student) {
        viewModelScope.launch {
            if (student.id == 0L) {
                val newId = repository.insertStudent(student)
                val createdStudent = student.copy(id = newId)
                isAddEditStudentOpen.value = false
                studentToEdit.value = null
                newlyEnrolledStudent.value = createdStudent
                isEnrollmentDialogOpen.value = true
                snackbarMessage.value = "Student '${student.name}' enrolled successfully!"
            } else {
                repository.updateStudent(student)
                isAddEditStudentOpen.value = false
                studentToEdit.value = null
                snackbarMessage.value = "Student updated successfully"
            }
        }
    }

    fun withdrawStudent(
        student: Student,
        withdrawalDate: String,
        reason: String,
        sendWhatsApp: Boolean,
        targetPhone: String,
        message: String,
        context: android.content.Context
    ) {
        viewModelScope.launch {
            repository.updateStudentWithdrawal(student.id, "WITHDRAWN", withdrawalDate, reason)
            isWithdrawalDialogOpen.value = false
            studentForWithdrawal.value = null
            if (selectedStudentDetail.value?.id == student.id) {
                selectedStudentDetail.value = student.copy(
                    enrollmentStatus = "WITHDRAWN",
                    withdrawalDate = withdrawalDate,
                    withdrawalReason = reason
                )
            }
            if (sendWhatsApp) {
                com.example.ui.components.WhatsAppHelper.sendWhatsAppMessage(context, targetPhone, message)
                snackbarMessage.value = "Student marked as Withdrawn. WhatsApp exit notice sent."
            } else {
                snackbarMessage.value = "Student marked as Withdrawn."
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            if (selectedStudentDetail.value?.id == student.id) {
                selectedStudentDetail.value = null
            }
            snackbarMessage.value = "Student '${student.name}' deleted"
        }
    }

    // Payment Operations
    fun collectFee(
        student: Student,
        amount: Double,
        discount: Double,
        monthYear: String,
        monthKey: Int,
        paymentMode: String,
        transactionId: String,
        remarks: String,
        onReceiptCreated: (FeePayment) -> Unit
    ) {
        viewModelScope.launch {
            val receiptNo = repository.generateNextReceiptNumber()
            val payment = FeePayment(
                studentId = student.id,
                studentName = student.name,
                gradeClass = student.gradeClass,
                monthYear = monthYear,
                monthKey = monthKey,
                amountPaid = amount,
                discount = discount,
                paymentDate = System.currentTimeMillis(),
                paymentMode = paymentMode,
                transactionId = transactionId,
                receiptNo = receiptNo,
                remarks = remarks
            )
            val paymentId = repository.recordPayment(payment)
            val savedPayment = payment.copy(id = paymentId)
            isPaymentDialogOpen.value = false
            studentForPayment.value = null
            selectedReceipt.value = savedPayment
            isReceiptDialogOpen.value = true
            snackbarMessage.value = "Payment recorded: Receipt $receiptNo"
            onReceiptCreated(savedPayment)
        }
    }

    fun deletePaymentRecord(payment: FeePayment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            if (selectedReceipt.value?.id == payment.id) {
                isReceiptDialogOpen.value = false
                selectedReceipt.value = null
            }
            snackbarMessage.value = "Receipt ${payment.receiptNo} deleted"
        }
    }

    // Attendance Operations
    fun loadAttendanceForDate(dateString: String) {
        viewModelScope.launch {
            selectedAttendanceDate.value = dateString
            repository.getAttendanceForDate(dateString).collect { list ->
                _attendanceList.value = list
            }
        }
    }

    fun markStudentAttendance(studentId: Long, status: String, batch: String) {
        viewModelScope.launch {
            val record = Attendance(
                studentId = studentId,
                dateString = selectedAttendanceDate.value,
                status = status,
                batch = batch
            )
            repository.markAttendance(record)
        }
    }

    fun markAllPresent(batch: String) {
        viewModelScope.launch {
            val studentList = students.value.filter { batch == "All" || it.batch == batch }
            val list = studentList.map {
                Attendance(
                    studentId = it.id,
                    dateString = selectedAttendanceDate.value,
                    status = "PRESENT",
                    batch = it.batch
                )
            }
            repository.markBatchAttendance(list)
            snackbarMessage.value = "Marked ${list.size} students as Present"
        }
    }

    // AI Operations
    fun generateAiReminder(
        record: StudentMonthlyFeeRecord,
        tone: String = "Polite & Gentle"
    ) {
        viewModelScope.launch {
            isAiGenerating.value = true
            aiReminderText.value = null
            val dueDateStr = "${record.student.dueDay}th of ${record.monthYear}"
            val result = geminiService.generateFeeReminder(
                studentName = record.student.name,
                parentName = "",
                month = record.monthYear,
                dueAmount = record.amountDue,
                dueDate = dueDateStr,
                tone = tone,
                instituteName = _settings.value.name,
                upiId = _settings.value.upiId
            )
            aiReminderText.value = result
            isAiGenerating.value = false
        }
    }

    fun generateInstituteAiInsights() {
        viewModelScope.launch {
            isAiGenerating.value = true
            aiInsightsText.value = null
            val result = geminiService.generateInstituteFeeInsights(
                students = students.value,
                payments = payments.value,
                currentMonth = _selectedMonthYear.value,
                instituteName = _settings.value.name
            )
            aiInsightsText.value = result
            isAiGenerating.value = false
        }
    }

    // Settings & PIN Lock
    fun saveInstituteSettings(newSettings: InstituteSettings) {
        repository.saveSettings(newSettings)
        _settings.value = newSettings
        snackbarMessage.value = "Settings updated successfully"
    }

    fun unlockWithPin(pin: String): Boolean {
        if (pin == _settings.value.adminPin) {
            _isPinLocked.value = false
            return true
        }
        return false
    }

    fun lockAdmin() {
        if (_settings.value.isPinProtectionEnabled && _settings.value.adminPin.isNotEmpty()) {
            _isPinLocked.value = true
        }
    }

    // Backup & Restore
    fun exportBackupJson(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            onExported(json)
            snackbarMessage.value = "Backup JSON generated successfully"
        }
    }

    fun restoreBackupJson(jsonString: String) {
        viewModelScope.launch {
            val result = repository.restoreBackupJson(jsonString)
            if (result.isSuccess) {
                _settings.value = repository.getSettings()
                snackbarMessage.value = result.getOrNull() ?: "Data restored successfully"
            } else {
                snackbarMessage.value = "Restore failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun exportStudentsCsv(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.exportStudentsCsv()
            onReady(csv)
        }
    }

    fun exportPaymentsCsv(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.exportPaymentsCsv()
            onReady(csv)
        }
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }
}
