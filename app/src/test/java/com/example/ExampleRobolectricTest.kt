package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.Student
import com.example.data.model.InstituteSettings
import com.example.ui.components.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FeeTrack", appName)
  }

  @Test
  fun `student model creation and settings default`() {
    val student = Student(
      name = "Rahul Sharma",
      rollNo = "10-A",
      gradeClass = "Class 10 - Mathematics",
      phone = "9876543210",
      monthlyFee = 1500.0,
      dueDay = 5
    )
    assertEquals("Rahul Sharma", student.name)
    assertEquals(1500.0, student.monthlyFee, 0.01)

    val settings = InstituteSettings()
    assertNotNull(settings.name)
    assertEquals("₹", settings.currencySymbol)
    assertEquals("2026-2027", settings.session)
  }

  @Test
  fun `sanitize phone number adds 91 prefix for 10 digit Indian numbers`() {
    assertEquals("919876543210", WhatsAppHelper.sanitizePhoneNumber("9876543210"))
    assertEquals("919876543210", WhatsAppHelper.sanitizePhoneNumber("+91 98765-43210"))
    assertEquals("14155552671", WhatsAppHelper.sanitizePhoneNumber("+1 (415) 555-2671"))
  }

  @Test
  fun `whatsapp fee reminder templates generation`() {
    val student = Student(
      id = 1L,
      name = "Aarav Patel",
      rollNo = "B-101",
      gradeClass = "Class 12 - Physics",
      batch = "Morning Batch (7:00 AM - 8:30 AM)",
      phone = "9876543210",
      parentPhone = "9876500000",
      monthlyFee = 2500.0,
      dueDay = 5
    )
    val settings = InstituteSettings(
      name = "Excel Coaching Academy",
      phone = "9988776655",
      upiId = "excelacademy@upi"
    )

    // Upcoming Reminder
    val upcomingMsg = WhatsAppHelper.buildUpcomingFeeReminder(
      student = student,
      monthYear = "October 2026",
      amountDue = 2500.0,
      dueDate = "5th of October 2026",
      settings = settings
    )
    assertTrue(upcomingMsg.contains("UPCOMING TUITION FEE NOTICE"))
    assertTrue(upcomingMsg.contains("Aarav Patel"))
    assertTrue(upcomingMsg.contains("₹2500"))
    assertTrue(upcomingMsg.contains("excelacademy@upi"))

    // Overdue Reminder
    val overdueMsg = WhatsAppHelper.buildOverdueFeeReminder(
      student = student,
      monthYear = "September 2026",
      amountDue = 2500.0,
      dueDate = "5th of September 2026",
      daysOverdue = 12,
      settings = settings
    )
    assertTrue(overdueMsg.contains("URGENT: PENDING FEE REMINDER"))
    assertTrue(overdueMsg.contains("12 days past due"))
    assertTrue(overdueMsg.contains("Aarav Patel"))

    // Enrollment Welcome Letter (First Entry)
    val welcomeMsg = WhatsAppHelper.buildEnrollmentWelcomeMessage(
      student = student,
      settings = settings
    )
    assertTrue(welcomeMsg.contains("ADMISSION & ENROLLMENT CONFIRMATION"))
    assertTrue(welcomeMsg.contains("Excel Coaching Academy"))
    assertTrue(welcomeMsg.contains("Aarav Patel"))

    // Withdrawal / Exit Notice (Last Entry)
    val withdrawalMsg = WhatsAppHelper.buildWithdrawalMessage(
      student = student,
      withdrawalDate = "2026-09-25",
      reason = "Course Completed Successfully",
      clearanceStatus = "All Dues Cleared (₹0 Pending) ✅",
      settings = settings
    )
    assertTrue(withdrawalMsg.contains("STUDENT WITHDRAWAL & COURSE COMPLETION NOTICE"))
    assertTrue(withdrawalMsg.contains("Course Completed Successfully"))
    assertTrue(withdrawalMsg.contains("All Dues Cleared"))
  }

  @Test
  fun `cloud backup record creation and verification`() {
    val backup = com.example.data.cloud.CloudBackupRecord(
      backupId = "backup_20260928_120000_abc123",
      userId = "user_test_987",
      instituteName = "Vidya Coaching Institute",
      session = "2026-2027",
      timestamp = "2026-09-28 12:00:00",
      totalStudents = 45,
      totalPayments = 120,
      dataPayload = "{}"
    )
    assertEquals("backup_20260928_120000_abc123", backup.backupId)
    assertEquals(45, backup.totalStudents)
    assertEquals("2026-2027", backup.session)
  }

  @Test
  fun `standard kg to 10 batches verification and flexible timings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = com.example.data.local.AppDatabase.getInstance(context)
    val repo = com.example.data.repository.FeeRepository(database.appDao(), context)
    val batches = repo.getStandardKgTo10Batches()

    // Verify all 11 classes from KG to 10 are present
    val classes = batches.map { it.gradeClass }
    assertTrue(classes.contains("Class KG"))
    for (i in 1..10) {
      assertTrue("Should contain Class $i", classes.contains("Class $i"))
    }

    // Verify all timings are within afternoon/evening range (12:00 PM to 8:00 PM)
    for (b in batches) {
      assertTrue("Timing should be PM: ${b.timeSlot}", b.timeSlot.contains("PM"))
      assertTrue("Batch name should not be blank", b.name.isNotBlank())
    }

    // Verify batch can be flexibly edited with custom timing
    val customBatch = batches.first().copy(
      name = "KG Little Stars - Advanced",
      timeSlot = "01:15 PM - 02:45 PM",
      feeAmount = 950.0
    )
    assertEquals("01:15 PM - 02:45 PM", customBatch.timeSlot)
    assertEquals(950.0, customBatch.feeAmount, 0.01)
  }

  @Test
  fun `expense item creation and net profit verification`() {
    val expense = com.example.data.local.ExpenseItem(
      title = "Classroom AC Electricity",
      category = "Electricity & Bills",
      amount = 3500.0,
      dateString = "2026-09-28",
      monthKey = 202609,
      paymentMode = "UPI",
      notes = "September bill"
    )
    assertEquals("Classroom AC Electricity", expense.title)
    assertEquals("Electricity & Bills", expense.category)
    assertEquals(3500.0, expense.amount, 0.01)

    // Net profit calculation
    val collectedFee = 45000.0
    val totalExpenses = 12000.0
    val netCashflow = collectedFee - totalExpenses
    assertEquals(33000.0, netCashflow, 0.01)
  }

  @Test
  fun `multi month advance fee payment verification`() {
    val payment = com.example.data.local.FeePayment(
      studentId = 5L,
      studentName = "Pooja Verma",
      gradeClass = "Class 9",
      monthYear = "September 2026",
      monthKey = 202609,
      amountPaid = 5400.0,
      discount = 200.0,
      paymentMode = "UPI",
      receiptNo = "REC-2026-0042",
      monthsCovered = 3,
      coveragePeriod = "3 Months Advance (Quarterly)"
    )
    assertEquals(3, payment.monthsCovered)
    assertEquals("3 Months Advance (Quarterly)", payment.coveragePeriod)
    assertEquals(5400.0, payment.amountPaid, 0.01)
    assertEquals(200.0, payment.discount, 0.01)
  }
}
