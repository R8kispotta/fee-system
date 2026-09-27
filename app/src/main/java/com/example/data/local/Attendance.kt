package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    indices = [Index(value = ["studentId", "dateString"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val dateString: String, // "YYYY-MM-DD"
    val status: String, // "PRESENT", "ABSENT", "LATE"
    val batch: String = ""
)
