package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "batches")
data class BatchItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gradeClass: String,
    val timeSlot: String = "",
    val feeAmount: Double = 0.0
)
