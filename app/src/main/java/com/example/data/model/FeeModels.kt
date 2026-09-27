package com.example.data.model

import com.example.data.local.FeePayment
import com.example.data.local.Student

enum class MonthFeeStatus {
    PAID,
    PARTIALLY_PAID,
    PENDING,
    OVERDUE,
    EXEMPT
}

data class StudentMonthlyFeeRecord(
    val student: Student,
    val monthYear: String,
    val monthKey: Int,
    val status: MonthFeeStatus,
    val amountDue: Double,
    val amountPaid: Double,
    val payment: FeePayment? = null,
    val isOverdue: Boolean = false,
    val daysOverdue: Int = 0
)

data class InstituteSettings(
    val name: String = "Vidya Coaching Institute",
    val tagline: String = "Empowering Students to Excel",
    val address: String = "Plot 42, Academy Lane, Knowledge Park",
    val phone: String = "+91 98765 43210",
    val email: String = "support@vidyainstitute.in",
    val upiId: String = "vidyainstitute@okaxis",
    val receiptPrefix: String = "REC",
    val currencySymbol: String = "₹",
    val adminPin: String = "",
    val isPinProtectionEnabled: Boolean = false,
    val themeMode: String = "system" // "system", "light", "dark"
)

data class DashboardSummary(
    val totalStudents: Int = 0,
    val collectedThisMonth: Double = 0.0,
    val pendingThisMonth: Double = 0.0,
    val totalExpectedThisMonth: Double = 0.0,
    val overdueCount: Int = 0,
    val overdueAmount: Double = 0.0,
    val collectionPercentage: Float = 0f,
    val selectedMonthYear: String = "",
    val selectedMonthKey: Int = 0
)
