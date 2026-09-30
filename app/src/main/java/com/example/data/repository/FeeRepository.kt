package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDao
import com.example.data.local.Attendance
import com.example.data.local.BatchItem
import com.example.data.local.ExpenseItem
import com.example.data.local.FeePayment
import com.example.data.local.Student
import com.example.data.model.InstituteSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FeeRepository(
    private val appDao: AppDao,
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("feetrack_prefs", Context.MODE_PRIVATE)

    val allStudents: Flow<List<Student>> = appDao.getAllStudents()
    val allPayments: Flow<List<FeePayment>> = appDao.getAllPayments()
    val allBatches: Flow<List<BatchItem>> = appDao.getAllBatches()
    val allExpenses: Flow<List<ExpenseItem>> = appDao.getAllExpenses()

    fun getExpensesForMonth(monthKey: Int): Flow<List<ExpenseItem>> = appDao.getExpensesForMonth(monthKey)

    suspend fun insertExpense(expense: ExpenseItem): Long = withContext(Dispatchers.IO) {
        appDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseItem) = withContext(Dispatchers.IO) {
        appDao.deleteExpense(expense)
    }

    fun getStudentById(id: Long): Flow<Student?> = appDao.getStudentById(id)

    suspend fun insertStudent(student: Student): Long = withContext(Dispatchers.IO) {
        appDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        appDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) = withContext(Dispatchers.IO) {
        appDao.deleteStudent(student)
    }

    suspend fun deleteStudentById(id: Long) = withContext(Dispatchers.IO) {
        appDao.deleteStudentById(id)
    }

    suspend fun updateStudentWithdrawal(studentId: Long, status: String, date: String, reason: String) = withContext(Dispatchers.IO) {
        appDao.updateWithdrawalStatus(studentId, status, date, reason)
    }

    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>> =
        appDao.getPaymentsForStudent(studentId)

    fun getPaymentsForMonth(monthKey: Int): Flow<List<FeePayment>> =
        appDao.getPaymentsForMonth(monthKey)

    suspend fun recordPayment(payment: FeePayment): Long = withContext(Dispatchers.IO) {
        appDao.insertPayment(payment)
    }

    suspend fun deletePayment(payment: FeePayment) = withContext(Dispatchers.IO) {
        appDao.deletePayment(payment)
    }

    fun getAttendanceForDate(dateString: String): Flow<List<Attendance>> =
        appDao.getAttendanceForDate(dateString)

    fun getAttendanceForStudent(studentId: Long): Flow<List<Attendance>> =
        appDao.getAttendanceForStudent(studentId)

    suspend fun markAttendance(attendance: Attendance): Long = withContext(Dispatchers.IO) {
        appDao.recordAttendance(attendance)
    }

    suspend fun markBatchAttendance(list: List<Attendance>) = withContext(Dispatchers.IO) {
        appDao.recordAttendanceList(list)
    }

    suspend fun insertBatch(batch: BatchItem): Long = withContext(Dispatchers.IO) {
        appDao.insertBatch(batch)
    }

    suspend fun updateBatch(batch: BatchItem) = withContext(Dispatchers.IO) {
        appDao.updateBatch(batch)
    }

    suspend fun deleteBatch(batch: BatchItem) = withContext(Dispatchers.IO) {
        appDao.deleteBatch(batch)
    }

    suspend fun deleteBatchById(id: Long) = withContext(Dispatchers.IO) {
        appDao.deleteBatchById(id)
    }

    fun getStandardKgTo10Batches(): List<BatchItem> {
        return listOf(
            BatchItem(name = "KG Little Stars", gradeClass = "Class KG", timeSlot = "12:00 PM - 01:30 PM", feeAmount = 800.0),
            BatchItem(name = "Class 1 Primers", gradeClass = "Class 1", timeSlot = "12:30 PM - 02:00 PM", feeAmount = 900.0),
            BatchItem(name = "Class 2 Juniors", gradeClass = "Class 2", timeSlot = "01:00 PM - 02:30 PM", feeAmount = 1000.0),
            BatchItem(name = "Class 3 Explorers", gradeClass = "Class 3", timeSlot = "01:30 PM - 03:00 PM", feeAmount = 1100.0),
            BatchItem(name = "Class 4 Achievers", gradeClass = "Class 4", timeSlot = "02:00 PM - 03:30 PM", feeAmount = 1200.0),
            BatchItem(name = "Class 5 Middle Wing", gradeClass = "Class 5", timeSlot = "02:30 PM - 04:00 PM", feeAmount = 1300.0),
            BatchItem(name = "Class 6 Foundation", gradeClass = "Class 6", timeSlot = "03:00 PM - 04:30 PM", feeAmount = 1400.0),
            BatchItem(name = "Class 7 Scholars", gradeClass = "Class 7", timeSlot = "04:00 PM - 05:30 PM", feeAmount = 1500.0),
            BatchItem(name = "Class 8 Pre-Boards", gradeClass = "Class 8", timeSlot = "04:30 PM - 06:00 PM", feeAmount = 1600.0),
            BatchItem(name = "Class 9 Secondary Prep", gradeClass = "Class 9", timeSlot = "05:00 PM - 06:30 PM", feeAmount = 1800.0),
            BatchItem(name = "Class 10 Board Masters", gradeClass = "Class 10", timeSlot = "06:30 PM - 08:00 PM", feeAmount = 2000.0)
        )
    }

    suspend fun seedKgTo10Batches(clearFirst: Boolean = false) = withContext(Dispatchers.IO) {
        if (clearFirst) {
            appDao.clearAllBatches()
        }
        appDao.insertBatches(getStandardKgTo10Batches())
    }

    // --- Settings Persistence ---
    fun getSettings(): InstituteSettings {
        return InstituteSettings(
            name = prefs.getString("institute_name", "Vidya Coaching Institute") ?: "Vidya Coaching Institute",
            tagline = prefs.getString("institute_tagline", "Empowering Students to Excel") ?: "Empowering Students to Excel",
            session = prefs.getString("institute_session", "2026-2027") ?: "2026-2027",
            address = prefs.getString("institute_address", "Plot 42, Academy Lane, Knowledge Park") ?: "Plot 42, Academy Lane, Knowledge Park",
            phone = prefs.getString("institute_phone", "+91 98765 43210") ?: "+91 98765 43210",
            email = prefs.getString("institute_email", "support@vidyainstitute.in") ?: "support@vidyainstitute.in",
            upiId = prefs.getString("institute_upi", "vidyainstitute@okaxis") ?: "vidyainstitute@okaxis",
            receiptPrefix = prefs.getString("institute_receipt_prefix", "REC") ?: "REC",
            currencySymbol = prefs.getString("institute_currency", "₹") ?: "₹",
            adminPin = prefs.getString("institute_admin_pin", "") ?: "",
            isPinProtectionEnabled = prefs.getBoolean("institute_pin_enabled", false),
            themeMode = prefs.getString("institute_theme_mode", "system") ?: "system"
        )
    }

    fun saveSettings(settings: InstituteSettings) {
        prefs.edit()
            .putString("institute_name", settings.name)
            .putString("institute_tagline", settings.tagline)
            .putString("institute_session", settings.session)
            .putString("institute_address", settings.address)
            .putString("institute_phone", settings.phone)
            .putString("institute_email", settings.email)
            .putString("institute_upi", settings.upiId)
            .putString("institute_receipt_prefix", settings.receiptPrefix)
            .putString("institute_currency", settings.currencySymbol)
            .putString("institute_admin_pin", settings.adminPin)
            .putBoolean("institute_pin_enabled", settings.isPinProtectionEnabled)
            .putString("institute_theme_mode", settings.themeMode)
            .apply()
    }

    // --- Generate Next Receipt Number ---
    suspend fun generateNextReceiptNumber(): String = withContext(Dispatchers.IO) {
        val settings = getSettings()
        val calendar = Calendar.getInstance()
        val yearMonth = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(calendar.time)
        val allPayments = appDao.getAllPayments().first()
        val seqNumber = (allPayments.size + 1).toString().padStart(4, '0')
        "${settings.receiptPrefix}-$yearMonth-$seqNumber"
    }

    // --- JSON Backup & Restore ---
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val students = appDao.getAllStudentsIncludingInactive().first()
        val payments = appDao.getAllPayments().first()
        val batches = appDao.getAllBatches().first()
        val settings = getSettings()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // Settings
        val settingsObj = JSONObject()
        settingsObj.put("name", settings.name)
        settingsObj.put("tagline", settings.tagline)
        settingsObj.put("session", settings.session)
        settingsObj.put("address", settings.address)
        settingsObj.put("phone", settings.phone)
        settingsObj.put("email", settings.email)
        settingsObj.put("upiId", settings.upiId)
        settingsObj.put("receiptPrefix", settings.receiptPrefix)
        settingsObj.put("currencySymbol", settings.currencySymbol)
        root.put("settings", settingsObj)

        // Students
        val studentsArr = JSONArray()
        for (s in students) {
            val item = JSONObject()
            item.put("id", s.id)
            item.put("name", s.name)
            item.put("rollNo", s.rollNo)
            item.put("gradeClass", s.gradeClass)
            item.put("batch", s.batch)
            item.put("phone", s.phone)
            item.put("parentPhone", s.parentPhone)
            item.put("monthlyFee", s.monthlyFee)
            item.put("dueDay", s.dueDay)
            item.put("admissionDate", s.admissionDate)
            item.put("photoUri", s.photoUri ?: "")
            item.put("active", s.active)
            item.put("enrollmentStatus", s.enrollmentStatus)
            item.put("withdrawalDate", s.withdrawalDate)
            item.put("withdrawalReason", s.withdrawalReason)
            item.put("notes", s.notes)
            studentsArr.put(item)
        }
        root.put("students", studentsArr)

        // Payments
        val paymentsArr = JSONArray()
        for (p in payments) {
            val item = JSONObject()
            item.put("id", p.id)
            item.put("studentId", p.studentId)
            item.put("studentName", p.studentName)
            item.put("gradeClass", p.gradeClass)
            item.put("monthYear", p.monthYear)
            item.put("monthKey", p.monthKey)
            item.put("amountPaid", p.amountPaid)
            item.put("discount", p.discount)
            item.put("paymentDate", p.paymentDate)
            item.put("paymentMode", p.paymentMode)
            item.put("transactionId", p.transactionId)
            item.put("receiptNo", p.receiptNo)
            item.put("remarks", p.remarks)
            paymentsArr.put(item)
        }
        root.put("payments", paymentsArr)

        // Batches
        val batchesArr = JSONArray()
        for (b in batches) {
            val item = JSONObject()
            item.put("id", b.id)
            item.put("name", b.name)
            item.put("gradeClass", b.gradeClass)
            item.put("timeSlot", b.timeSlot)
            item.put("feeAmount", b.feeAmount)
            batchesArr.put(item)
        }
        root.put("batches", batchesArr)

        root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                val current = getSettings()
                saveSettings(
                    current.copy(
                        name = s.optString("name", current.name),
                        tagline = s.optString("tagline", current.tagline),
                        session = s.optString("session", current.session),
                        address = s.optString("address", current.address),
                        phone = s.optString("phone", current.phone),
                        email = s.optString("email", current.email),
                        upiId = s.optString("upiId", current.upiId),
                        receiptPrefix = s.optString("receiptPrefix", current.receiptPrefix),
                        currencySymbol = s.optString("currencySymbol", current.currencySymbol)
                    )
                )
            }

            var studentCount = 0
            if (root.has("students")) {
                val studentsArr = root.getJSONArray("students")
                val studentsList = mutableListOf<Student>()
                for (i in 0 until studentsArr.length()) {
                    val obj = studentsArr.getJSONObject(i)
                    studentsList.add(
                        Student(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            rollNo = obj.optString("rollNo", ""),
                            gradeClass = obj.getString("gradeClass"),
                            batch = obj.optString("batch", "Regular Batch"),
                            phone = obj.getString("phone"),
                            parentPhone = obj.optString("parentPhone", ""),
                            monthlyFee = obj.getDouble("monthlyFee"),
                            dueDay = obj.optInt("dueDay", 5),
                            admissionDate = obj.optString("admissionDate", ""),
                            photoUri = obj.optString("photoUri", "").takeIf { it.isNotEmpty() },
                            active = obj.optBoolean("active", true),
                            enrollmentStatus = obj.optString("enrollmentStatus", "ACTIVE"),
                            withdrawalDate = obj.optString("withdrawalDate", ""),
                            withdrawalReason = obj.optString("withdrawalReason", ""),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                appDao.insertStudents(studentsList)
                studentCount = studentsList.size
            }

            var paymentCount = 0
            if (root.has("payments")) {
                val paymentsArr = root.getJSONArray("payments")
                val paymentsList = mutableListOf<FeePayment>()
                for (i in 0 until paymentsArr.length()) {
                    val obj = paymentsArr.getJSONObject(i)
                    paymentsList.add(
                        FeePayment(
                            id = obj.optLong("id", 0),
                            studentId = obj.getLong("studentId"),
                            studentName = obj.getString("studentName"),
                            gradeClass = obj.optString("gradeClass", ""),
                            monthYear = obj.getString("monthYear"),
                            monthKey = obj.getInt("monthKey"),
                            amountPaid = obj.getDouble("amountPaid"),
                            discount = obj.optDouble("discount", 0.0),
                            paymentDate = obj.optLong("paymentDate", System.currentTimeMillis()),
                            paymentMode = obj.optString("paymentMode", "Cash"),
                            transactionId = obj.optString("transactionId", ""),
                            receiptNo = obj.getString("receiptNo"),
                            remarks = obj.optString("remarks", "")
                        )
                    )
                }
                appDao.insertPayments(paymentsList)
                paymentCount = paymentsList.size
            }

            if (root.has("batches")) {
                val batchesArr = root.getJSONArray("batches")
                val batchesList = mutableListOf<BatchItem>()
                for (i in 0 until batchesArr.length()) {
                    val obj = batchesArr.getJSONObject(i)
                    batchesList.add(
                        BatchItem(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            gradeClass = obj.getString("gradeClass"),
                            timeSlot = obj.optString("timeSlot", ""),
                            feeAmount = obj.optDouble("feeAmount", 0.0)
                        )
                    )
                }
                appDao.insertBatches(batchesList)
            }

            Result.success("Restored $studentCount students & $paymentCount payment records successfully.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CSV Export for Excel ---
    suspend fun exportStudentsCsv(): String = withContext(Dispatchers.IO) {
        val students = appDao.getAllStudents().first()
        val sb = StringBuilder()
        sb.append("ID,Name,Roll No,Class/Grade,Batch,Phone,Parent Phone,Monthly Fee,Due Day,Admission Date,Notes\n")
        for (s in students) {
            sb.append("${s.id},\"${s.name.replace("\"", "\"\"")}\",\"${s.rollNo}\",\"${s.gradeClass}\",\"${s.batch}\",\"${s.phone}\",\"${s.parentPhone}\",${s.monthlyFee},${s.dueDay},\"${s.admissionDate}\",\"${s.notes.replace("\"", "\"\"")}\"\n")
        }
        sb.toString()
    }

    suspend fun exportPaymentsCsv(): String = withContext(Dispatchers.IO) {
        val payments = appDao.getAllPayments().first()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("Receipt No,Student ID,Student Name,Class,Fee Month,Amount Paid,Discount,Payment Mode,Transaction ID,Payment Date,Remarks\n")
        for (p in payments) {
            val dateStr = dateFormat.format(Date(p.paymentDate))
            sb.append("\"${p.receiptNo}\",${p.studentId},\"${p.studentName}\",\"${p.gradeClass}\",\"${p.monthYear}\",${p.amountPaid},${p.discount},\"${p.paymentMode}\",\"${p.transactionId}\",\"$dateStr\",\"${p.remarks.replace("\"", "\"\"")}\"\n")
        }
        sb.toString()
    }

    // --- Prepopulate Sample Data if Clean DB ---
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val existingBatches = appDao.getAllBatches().first()
        val hasKgBatches = existingBatches.any { it.gradeClass.contains("KG", ignoreCase = true) }
        if (existingBatches.isEmpty() || !hasKgBatches) {
            appDao.insertBatches(getStandardKgTo10Batches())
        }

        val existingExpenses = appDao.getAllExpenses().first()
        if (existingExpenses.isEmpty()) {
            val currentMonthKey = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date()).toInt()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val sampleExpenses = listOf(
                ExpenseItem(title = "Premises Rent", category = "Rent", amount = 12000.0, dateString = today, monthKey = currentMonthKey, paymentMode = "Bank Transfer", notes = "Monthly institute lease"),
                ExpenseItem(title = "Electricity & Power", category = "Utilities", amount = 1850.0, dateString = today, monthKey = currentMonthKey, paymentMode = "UPI", notes = "Air conditioning & lights"),
                ExpenseItem(title = "Study Notes & Test Printing", category = "Printing", amount = 1400.0, dateString = today, monthKey = currentMonthKey, paymentMode = "Cash", notes = "Class 9 & 10 study booklets"),
                ExpenseItem(title = "Broadband Internet", category = "Utilities", amount = 999.0, dateString = today, monthKey = currentMonthKey, paymentMode = "UPI", notes = "High speed WiFi")
            )
            for (e in sampleExpenses) {
                appDao.insertExpense(e)
            }
        }

        val existingStudents = appDao.getAllStudents().first()
        if (existingStudents.isEmpty()) {

            val calendar = Calendar.getInstance()
            val currentMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            val currentMonthKey = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(calendar.time).toInt()

            calendar.add(Calendar.MONTH, -1)
            val lastMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            val lastMonthKey = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(calendar.time).toInt()

            val s1 = Student(
                name = "Aarav Sharma",
                rollNo = "10A-01",
                gradeClass = "Class 10 - Mathematics",
                batch = "Morning Batch (Board Prep)",
                phone = "9876543211",
                parentPhone = "9876543201",
                monthlyFee = 1800.0,
                dueDay = 5,
                admissionDate = "2026-06-10",
                notes = "Strong in algebra, preparing for NTSE"
            )
            val s2 = Student(
                name = "Priya Patel",
                rollNo = "12P-04",
                gradeClass = "Class 12 - Physics",
                batch = "Evening Batch",
                phone = "9876543212",
                parentPhone = "9876543202",
                monthlyFee = 2500.0,
                dueDay = 10,
                admissionDate = "2026-05-15",
                notes = "Preparing for JEE Mains"
            )
            val s3 = Student(
                name = "Rohan Verma",
                rollNo = "11S-09",
                gradeClass = "Class 11 - Science",
                batch = "JEE / NEET Foundation",
                phone = "9876543213",
                parentPhone = "9876543203",
                monthlyFee = 3000.0,
                dueDay = 5,
                admissionDate = "2026-06-01",
                notes = "Consistent performer"
            )
            val s4 = Student(
                name = "Ananya Singh",
                rollNo = "10A-08",
                gradeClass = "Class 10 - Mathematics",
                batch = "Morning Batch (Board Prep)",
                phone = "9876543214",
                parentPhone = "9876543204",
                monthlyFee = 1800.0,
                dueDay = 7,
                admissionDate = "2026-06-12",
                notes = "Needs regular trigonometry practice"
            )
            val s5 = Student(
                name = "Kunal Mukherjee",
                rollNo = "ENG-03",
                gradeClass = "Language & Skill",
                batch = "Grammar & Spoken English",
                phone = "9876543215",
                parentPhone = "9876543205",
                monthlyFee = 1200.0,
                dueDay = 5,
                admissionDate = "2026-07-01",
                notes = "Enrolled for spoken English & GD"
            )
            val s6 = Student(
                name = "Tanvi Kulkarni",
                rollNo = "12P-11",
                gradeClass = "Class 12 - Physics",
                batch = "Evening Batch",
                phone = "9876543216",
                parentPhone = "9876543206",
                monthlyFee = 2500.0,
                dueDay = 1,
                admissionDate = "2026-05-20",
                notes = "Fee pending for last 2 months"
            )

            val id1 = appDao.insertStudent(s1)
            val id2 = appDao.insertStudent(s2)
            val id3 = appDao.insertStudent(s3)
            val id4 = appDao.insertStudent(s4)
            val id5 = appDao.insertStudent(s5)
            val id6 = appDao.insertStudent(s6)

            // Seed some payments
            val p1 = FeePayment(
                studentId = id1,
                studentName = s1.name,
                gradeClass = s1.gradeClass,
                monthYear = currentMonthYear,
                monthKey = currentMonthKey,
                amountPaid = 1800.0,
                discount = 0.0,
                paymentDate = System.currentTimeMillis() - 86400000L * 3,
                paymentMode = "UPI",
                transactionId = "UPI-9284729103",
                receiptNo = "REC-${currentMonthKey}-0001",
                remarks = "Paid via Google Pay"
            )
            val p2 = FeePayment(
                studentId = id2,
                studentName = s2.name,
                gradeClass = s2.gradeClass,
                monthYear = currentMonthYear,
                monthKey = currentMonthKey,
                amountPaid = 2500.0,
                discount = 0.0,
                paymentDate = System.currentTimeMillis() - 86400000L * 5,
                paymentMode = "Cash",
                transactionId = "",
                receiptNo = "REC-${currentMonthKey}-0002",
                remarks = "Received in cash at office"
            )
            val p3 = FeePayment(
                studentId = id3,
                studentName = s3.name,
                gradeClass = s3.gradeClass,
                monthYear = lastMonthYear,
                monthKey = lastMonthKey,
                amountPaid = 3000.0,
                discount = 0.0,
                paymentDate = System.currentTimeMillis() - 86400000L * 25,
                paymentMode = "Bank Transfer",
                transactionId = "NEFT-88392019",
                receiptNo = "REC-${lastMonthKey}-0015",
                remarks = "Net banking transfer"
            )
            val p4 = FeePayment(
                studentId = id5,
                studentName = s5.name,
                gradeClass = s5.gradeClass,
                monthYear = currentMonthYear,
                monthKey = currentMonthKey,
                amountPaid = 1200.0,
                discount = 0.0,
                paymentDate = System.currentTimeMillis() - 86400000L * 1,
                paymentMode = "UPI",
                transactionId = "UPI-7749204910",
                receiptNo = "REC-${currentMonthKey}-0003",
                remarks = "PhonePe payment"
            )

            appDao.insertPayments(listOf(p1, p2, p3, p4))
        }
    }
}
