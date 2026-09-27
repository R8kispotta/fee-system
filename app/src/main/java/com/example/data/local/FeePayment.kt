package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fee_payments")
data class FeePayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val gradeClass: String = "",
    val monthYear: String, // e.g. "September 2026"
    val monthKey: Int, // e.g. 202609 for sorting and matching
    val amountPaid: Double,
    val discount: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMode: String = "Cash", // "Cash", "UPI", "Bank Transfer", "Cheque"
    val transactionId: String = "",
    val receiptNo: String,
    val remarks: String = ""
)
