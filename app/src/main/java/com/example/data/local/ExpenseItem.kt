package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "institute_expenses")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Rent", "Faculty Salary", "Electricity & Bills", "Study Material & Printing", "Maintenance", "Miscellaneous"
    val amount: Double,
    val dateString: String, // e.g. "2026-09-28"
    val monthKey: Int, // e.g. 202609
    val paymentMode: String = "Cash", // "Cash", "UPI", "Bank Transfer"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
