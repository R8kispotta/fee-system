package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Students ---
    @Query("SELECT * FROM students WHERE active = 1 ORDER BY CASE WHEN enrollmentStatus = 'ACTIVE' THEN 0 ELSE 1 END, name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY CASE WHEN enrollmentStatus = 'ACTIVE' THEN 0 ELSE 1 END, name ASC")
    fun getAllStudentsIncludingInactive(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentByIdSync(id: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Query("UPDATE students SET enrollmentStatus = :status, withdrawalDate = :date, withdrawalReason = :reason WHERE id = :studentId")
    suspend fun updateWithdrawalStatus(studentId: Long, status: String, date: String, reason: String)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    // --- Payments ---
    @Query("SELECT * FROM fee_payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE monthKey = :monthKey ORDER BY paymentDate DESC")
    fun getPaymentsForMonth(monthKey: Int): Flow<List<FeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: FeePayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<FeePayment>)

    @Delete
    suspend fun deletePayment(payment: FeePayment)

    @Query("DELETE FROM fee_payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    // --- Attendance ---
    @Query("SELECT * FROM attendance WHERE dateString = :dateString")
    fun getAttendanceForDate(dateString: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId")
    fun getAttendanceForStudent(studentId: Long): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAttendance(attendance: Attendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAttendanceList(attendanceList: List<Attendance>)

    // --- Batches ---
    @Query("SELECT * FROM batches ORDER BY id ASC")
    fun getAllBatches(): Flow<List<BatchItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<BatchItem>)

    @Update
    suspend fun updateBatch(batch: BatchItem)

    @Delete
    suspend fun deleteBatch(batch: BatchItem)

    @Query("DELETE FROM batches WHERE id = :id")
    suspend fun deleteBatchById(id: Long)

    @Query("DELETE FROM batches")
    suspend fun clearAllBatches()

    // --- Expenses ---
    @Query("SELECT * FROM institute_expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseItem>>

    @Query("SELECT * FROM institute_expenses WHERE monthKey = :monthKey ORDER BY createdAt DESC")
    fun getExpensesForMonth(monthKey: Int): Flow<List<ExpenseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseItem): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseItem)

    @Query("DELETE FROM institute_expenses")
    suspend fun clearAllExpenses()

    // Bulk cleanup
    @Query("DELETE FROM students")
    suspend fun clearAllStudents()

    @Query("DELETE FROM fee_payments")
    suspend fun clearAllPayments()

    @Query("DELETE FROM attendance")
    suspend fun clearAllAttendance()
}
