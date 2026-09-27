package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rollNo: String = "",
    val gradeClass: String, // e.g. "Class 10 - Mathematics"
    val batch: String = "Regular Batch", // e.g. "Morning 8:00 AM"
    val phone: String,
    val parentPhone: String = "",
    val monthlyFee: Double,
    val dueDay: Int = 5, // Day of month when fee becomes due
    val admissionDate: String = "",
    val photoUri: String? = null,
    val active: Boolean = true,
    val enrollmentStatus: String = "ACTIVE", // "ACTIVE", "WITHDRAWN", "COMPLETED"
    val withdrawalDate: String = "",
    val withdrawalReason: String = "", // e.g. "Course Completed", "Relocated", "Personal"
    val notes: String = ""
)
