package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.FeePayment
import com.example.data.local.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiFeeService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateFeeReminder(
        studentName: String,
        parentName: String,
        month: String,
        dueAmount: Double,
        dueDate: String,
        tone: String, // "Polite & Gentle", "Formal / Official", "Firm & Urgent", "Hinglish Friendly"
        instituteName: String,
        upiId: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are an administrative assistant for '$instituteName', a coaching institute.
            Write a WhatsApp fee reminder message for:
            - Student Name: $studentName
            - Parent/Guardian: ${parentName.ifEmpty { "Guardian" }}
            - Fee Month: $month
            - Pending Amount: ₹$dueAmount
            - Due Date: $dueDate
            - UPI Payment ID: $upiId
            - Selected Tone: $tone
            
            Guidelines:
            1. Keep it clear, respectful, professional, and ready to send directly on WhatsApp.
            2. Include relevant emojis (📚, 💳, ✅, 🗓️, 🙏).
            3. Highlight the exact pending amount and due date.
            4. Include a polite closing and the institute's name and UPI ID.
            5. Do NOT include markdown code blocks or explanations—output ONLY the final message text.
        """.trimIndent()

        val response = callGemini(prompt)
        if (response.isNotEmpty()) {
            response
        } else {
            // High-quality smart template fallback if API key isn't provided or offline
            getFallbackReminder(studentName, month, dueAmount, dueDate, instituteName, upiId, tone)
        }
    }

    suspend fun generateInstituteFeeInsights(
        students: List<Student>,
        payments: List<FeePayment>,
        currentMonth: String,
        instituteName: String
    ): String = withContext(Dispatchers.IO) {
        val totalStudents = students.size
        val totalExpected = students.sumOf { it.monthlyFee }
        val collectedThisMonth = payments.filter { it.monthYear.equals(currentMonth, ignoreCase = true) }
            .sumOf { it.amountPaid }
        val pendingAmount = (totalExpected - collectedThisMonth).coerceAtLeast(0.0)
        val collectionRate = if (totalExpected > 0) ((collectedThisMonth / totalExpected) * 100).toInt() else 0

        val prompt = """
            You are a senior financial and administrative advisor for educational coaching institutes.
            Analyze the following monthly fee status for '$instituteName':
            - Current Month: $currentMonth
            - Total Active Students: $totalStudents
            - Total Expected Monthly Fees: ₹$totalExpected
            - Total Collected So Far: ₹$collectedThisMonth ($collectionRate%)
            - Pending Fee Dues: ₹$pendingAmount
            - Batch breakdown: ${students.groupBy { it.batch }.map { "${it.key}: ${it.value.size} students" }.joinToString("; ")}
            
            Provide a concise, highly actionable 3-part advisory report:
            1. 📊 Executive Health Check (Collection progress and status assessment)
            2. 🎯 High-Priority Recovery Actions (Immediate steps to recover pending ₹$pendingAmount)
            3. 💡 Batch & Revenue Growth Strategy (Tips to streamline fee collection, reduce defaults, and improve retention)
            
            Keep formatting crisp with clean bullet points and emojis. No markdown code blocks.
        """.trimIndent()

        val response = callGemini(prompt)
        if (response.isNotEmpty()) {
            response
        } else {
            """
            📊 Executive Health Check ($currentMonth)
            • Total Active Students: $totalStudents
            • Collected: ₹$collectedThisMonth ($collectionRate% of target)
            • Remaining Dues: ₹$pendingAmount

            🎯 High-Priority Recovery Actions:
            • Send targeted 1-click WhatsApp reminders to students with dues past their designated due date.
            • Offer flexible UPI or digital payment options with instant receipt generation.
            • Follow up personally with guardians whose fees are more than 15 days overdue.

            💡 Revenue & Collection Strategy:
            • Implement early-bird discount (e.g. 5% off if paid before the 5th of every month).
            • Group collections by batches so batch mentors can gently remind students after class.
            • Keep digital attendance linked with fee tracking to identify irregular attendees early.
            """.trimIndent()
        }
    }

    suspend fun generateStudentReport(
        student: Student,
        payments: List<FeePayment>,
        attendanceSummary: String,
        instituteName: String
    ): String = withContext(Dispatchers.IO) {
        val studentPayments = payments.filter { it.studentId == student.id }
        val prompt = """
            Create a professional and encouraging Student Status & Fee Clearance Summary for '$instituteName':
            - Student: ${student.name} (Roll: ${student.rollNo})
            - Class & Batch: ${student.gradeClass} (${student.batch})
            - Monthly Tuition Fee: ₹${student.monthlyFee}
            - Total Payments Recorded: ${studentPayments.size} receipts
            - Attendance Status: $attendanceSummary
            - Teacher's Observations: ${student.notes.ifEmpty { "Active classroom engagement" }}

            Format as a clean, ready-to-share card with:
            • Student Overview & Academic Commitment
            • Attendance & Regularity
            • Fee Standing (Cleared / Pending)
            • Personalized Mentorship Note for Parents
        """.trimIndent()

        val response = callGemini(prompt)
        if (response.isNotEmpty()) {
            response
        } else {
            """
            🎓 Student Status Report — $instituteName
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            👤 Student: ${student.name} (${student.rollNo})
            📚 Class: ${student.gradeClass} | Batch: ${student.batch}
            
            📈 Attendance: $attendanceSummary
            💳 Monthly Fee: ₹${student.monthlyFee}
            📝 Notes: ${student.notes.ifEmpty { "Consistent and regular learner." }}
            
            🌟 Teacher's Feedback:
            ${student.name} is making steady progress in curriculum milestones. Please ensure timely fee settlement and continued guidance at home to support regular academic growth.
            """.trimIndent()
        }
    }

    private suspend fun callGemini(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiFeeService", "API Key not configured or placeholder, returning fallback")
            return@withContext ""
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GeminiFeeService", "API call failed with code: ${response.code}")
                    return@withContext ""
                }
                val bodyStr = response.body?.string() ?: return@withContext ""
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "").trim()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiFeeService", "Error calling Gemini API: ${e.message}", e)
        }
        ""
    }

    private fun getFallbackReminder(
        studentName: String,
        month: String,
        dueAmount: Double,
        dueDate: String,
        instituteName: String,
        upiId: String,
        tone: String
    ): String {
        return when {
            tone.contains("Firm", ignoreCase = true) -> """
                ⚠️ *IMPORTANT FEE NOTICE*
                Dear Parent/Guardian of *$studentName*,
                
                This is a reminder regarding the pending tuition fee for *$month* at *$instituteName*.
                
                📌 *Pending Amount:* ₹$dueAmount
                🗓️ *Due Date:* $dueDate
                
                Please clear the dues at the earliest via UPI to avoid any disruption in regular classes:
                💳 *UPI ID:* `$upiId`
                
                If already paid, kindly share the screenshot or receipt. Thank you!
                — *$instituteName Administration*
            """.trimIndent()

            tone.contains("Hinglish", ignoreCase = true) -> """
                Namaste! 🙏
                *$instituteName* se yeh reminder hai *$studentName* ki tuition fees ke baare mein.
                
                📚 *Month:* $month
                💰 *Amount Due:* ₹$dueAmount
                🗓️ *Due Date:* $dueDate
                
                Aap aasaani se UPI se pay kar sakte hain:
                📱 *UPI ID:* `$upiId`
                
                Payment ke baad please screenshot share kar dijiye receipt ke liye. Dhanyawaad!
            """.trimIndent()

            else -> """
                Dear Parent/Guardian of *$studentName*,
                
                Greetings from *$instituteName*! 🎓
                
                This is a gentle reminder regarding the tuition fee for the month of *$month*.
                
                💰 *Amount Due:* ₹$dueAmount
                📅 *Due Date:* $dueDate
                
                You can conveniently pay online via UPI:
                💳 *UPI ID:* `$upiId`
                
                Your prompt support helps us maintain uninterrupted academic excellence. If already paid, please ignore this message.
                
                Warm regards,
                *$instituteName*
            """.trimIndent()
        }
    }
}
