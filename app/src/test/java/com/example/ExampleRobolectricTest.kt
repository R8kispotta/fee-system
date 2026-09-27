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
}
