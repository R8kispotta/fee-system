package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.local.Student
import com.example.data.model.InstituteSettings
import java.net.URLEncoder

object WhatsAppHelper {

    /**
     * Clean phone number: remove spaces, dashes, parentheses.
     * If 10 digits, prefix with '91' (default India country code).
     */
    fun sanitizePhoneNumber(rawPhone: String): String {
        var clean = rawPhone.replace(Regex("[^0-9]"), "")
        if (clean.length == 10) {
            clean = "91$clean"
        }
        return clean
    }

    /**
     * Dispatches a WhatsApp message using standard Intent.
     * Falls back smoothly to SMS if WhatsApp client is not present.
     */
    fun sendWhatsAppMessage(
        context: Context,
        rawPhone: String,
        message: String,
        fallbackToSms: Boolean = true
    ) {
        val cleanPhone = sanitizePhoneNumber(rawPhone)
        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = if (cleanPhone.isNotEmpty()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            if (fallbackToSms && cleanPhone.isNotEmpty()) {
                Toast.makeText(context, "Opening SMS app as fallback...", Toast.LENGTH_SHORT).show()
                sendSmsMessage(context, cleanPhone, message)
            } else {
                Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Fallback to direct SMS if needed
     */
    fun sendSmsMessage(
        context: Context,
        phone: String,
        message: String
    ) {
        try {
            val uri = Uri.parse("smsto:$phone")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open messaging app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Message 1: Upcoming Fee Reminder (Friendly & timely)
     */
    fun buildUpcomingFeeReminder(
        student: Student,
        monthYear: String,
        amountDue: Double,
        dueDate: String,
        settings: InstituteSettings
    ): String {
        return """
        🔔 *UPCOMING TUITION FEE NOTICE*
        ━━━━━━━━━━━━━━━━━━━━━
        Dear Parent / Student of *${student.name}*,
        
        Greetings from *${settings.name}*! 🎓
        
        This is a gentle reminder that the tuition fee for the upcoming month is due soon.
        
        👤 *Student:* ${student.name} ${if (student.rollNo.isNotEmpty()) "(${student.rollNo})" else ""}
        📚 *Class / Subject:* ${student.gradeClass}
        🗓️ *Fee Month:* $monthYear
        💰 *Amount Due:* ${settings.currencySymbol}${amountDue.toInt()}
        📅 *Due Date:* $dueDate
        
        💳 *UPI Payment ID:*
        `${settings.upiId}`
        
        Kindly settle the payment on or before the due date to ensure uninterrupted classes and materials.
        
        If already paid, please share the screenshot for receipt generation. Thank you! 🙏
        ━━━━━━━━━━━━━━━━━━━━━
        📞 *Helpline / Office:* ${settings.phone}
        📍 *${settings.name}*
        """.trimIndent()
    }

    /**
     * Message 2: Overdue Fee Alert (Urgent & Clear)
     */
    fun buildOverdueFeeReminder(
        student: Student,
        monthYear: String,
        amountDue: Double,
        dueDate: String,
        daysOverdue: Int,
        settings: InstituteSettings
    ): String {
        val overdueNote = if (daysOverdue > 0) "($daysOverdue days past due)" else "(Past due date)"
        return """
        ⚠️ *URGENT: PENDING FEE REMINDER*
        ━━━━━━━━━━━━━━━━━━━━━
        Dear Parent / Guardian of *${student.name}*,
        
        This is an important reminder regarding pending tuition fees at *${settings.name}*.
        
        👤 *Student Name:* ${student.name}
        📋 *Roll No:* ${student.rollNo.ifEmpty { "—" }}
        📚 *Course / Batch:* ${student.gradeClass} (${student.batch})
        🗓️ *Pending Month:* $monthYear
        🚨 *Status:* OVERDUE $overdueNote
        
        💰 *Total Due Amount:* ${settings.currencySymbol}${amountDue.toInt()}
        📅 *Scheduled Due Date was:* $dueDate
        
        Please clear the pending dues today to maintain active enrollment and test eligibility:
        📱 *Pay via UPI:*
        `${settings.upiId}`
        
        Kindly reply with the transaction reference or payment screenshot once transferred.
        ━━━━━━━━━━━━━━━━━━━━━
        For queries or fee assistance:
        📞 *Admin Desk:* ${settings.phone}
        🏛️ *${settings.name}*
        """.trimIndent()
    }

    /**
     * Message 3: Enrollment Welcome Confirmation (First Entry)
     */
    fun buildEnrollmentWelcomeMessage(
        student: Student,
        settings: InstituteSettings
    ): String {
        return """
        🎉 *ADMISSION & ENROLLMENT CONFIRMATION*
        ━━━━━━━━━━━━━━━━━━━━━
        Welcome to *${settings.name}*! 🎓
        
        We are delighted to confirm the successful enrollment of *${student.name}*.
        
        📋 *Student ID / Roll No:* ${student.rollNo.ifEmpty { "Assigned" }}
        📚 *Course / Subject:* ${student.gradeClass}
        ⏰ *Batch & Timing:* ${student.batch}
        📅 *Admission Date:* ${student.admissionDate.ifEmpty { "Today" }}
        
        💳 *Tuition Fee Structure:*
        • Monthly Tuition: ${settings.currencySymbol}${student.monthlyFee.toInt()}
        • Monthly Due Date: ${student.dueDay}th of each month
        • Institute UPI ID: `${settings.upiId}`
        
        🌟 *Our Commitment:*
        We are committed to providing top-quality concept clarity, regular practice tests, and individual attention to foster academic success.
        
        📍 *Campus / Center:* ${settings.address}
        📞 *Support / Updates:* ${settings.phone}
        ━━━━━━━━━━━━━━━━━━━━━
        Best wishes for an inspiring academic year ahead! 🚀
        """.trimIndent()
    }

    /**
     * Message 4: Withdrawal / Completion Notice (Last Entry)
     */
    fun buildWithdrawalMessage(
        student: Student,
        withdrawalDate: String,
        reason: String,
        clearanceStatus: String,
        settings: InstituteSettings
    ): String {
        return """
        📜 *STUDENT WITHDRAWAL & COURSE COMPLETION NOTICE*
        ━━━━━━━━━━━━━━━━━━━━━
        Official Notice from *${settings.name}*
        
        This message confirms the completion / withdrawal of enrollment for the following student:
        
        👤 *Student Name:* ${student.name}
        📋 *Roll No:* ${student.rollNo.ifEmpty { "—" }}
        📚 *Course / Class:* ${student.gradeClass}
        ⏰ *Batch:* ${student.batch}
        🗓️ *Effective Date:* $withdrawalDate
        
        📌 *Exit Category / Reason:* $reason
        💳 *Fee & Clearance Status:* $clearanceStatus
        
        ━━━━━━━━━━━━━━━━━━━━━
        We thank you for having been an esteemed part of *${settings.name}*. We sincerely wish *${student.name}* the absolute greatest success in all upcoming examinations, future education, and career milestones! 🌟
        
        With warm regards,
        🏛️ *Administration Office*
        *${settings.name}*
        📞 ${settings.phone}
        """.trimIndent()
    }
}
