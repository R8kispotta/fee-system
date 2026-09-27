package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.local.FeePayment
import com.example.data.local.Student
import com.example.data.model.InstituteSettings
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrintReceiptHelper {

    fun generateReceiptHtml(
        payment: FeePayment,
        student: Student?,
        settings: InstituteSettings
    ): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(payment.paymentDate))

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Fee Receipt - ${payment.receiptNo}</title>
            <style>
                body {
                    font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                    margin: 0;
                    padding: 24px;
                    color: #1e293b;
                    background-color: #ffffff;
                }
                .receipt-container {
                    max-width: 600px;
                    margin: 0 auto;
                    border: 2px solid #0f172a;
                    border-radius: 12px;
                    padding: 24px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
                }
                .header {
                    text-align: center;
                    border-bottom: 2px dashed #cbd5e1;
                    padding-bottom: 16px;
                    margin-bottom: 20px;
                }
                .institute-title {
                    font-size: 24px;
                    font-weight: 800;
                    color: #1e3a8a;
                    margin: 0 0 4px 0;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                }
                .institute-tagline {
                    font-size: 13px;
                    color: #0d9488;
                    margin: 0 0 6px 0;
                    font-weight: 600;
                }
                .institute-details {
                    font-size: 12px;
                    color: #64748b;
                    margin: 0;
                    line-height: 1.4;
                }
                .receipt-badge {
                    display: inline-block;
                    background-color: #10b981;
                    color: white;
                    font-weight: bold;
                    padding: 4px 12px;
                    border-radius: 20px;
                    font-size: 13px;
                    margin-top: 10px;
                    letter-spacing: 0.5px;
                }
                .meta-table {
                    width: 100%;
                    margin-bottom: 20px;
                    font-size: 13px;
                }
                .meta-table td {
                    padding: 4px 0;
                }
                .label {
                    color: #64748b;
                    font-weight: 600;
                    width: 35%;
                }
                .val {
                    color: #0f172a;
                    font-weight: 700;
                }
                .items-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 10px;
                    margin-bottom: 20px;
                }
                .items-table th {
                    background-color: #f1f5f9;
                    text-align: left;
                    padding: 8px 12px;
                    font-size: 12px;
                    color: #334155;
                    border-top: 1px solid #cbd5e1;
                    border-bottom: 1px solid #cbd5e1;
                }
                .items-table td {
                    padding: 10px 12px;
                    font-size: 13px;
                    border-bottom: 1px solid #e2e8f0;
                }
                .total-row td {
                    font-size: 15px;
                    font-weight: 800;
                    border-top: 2px solid #0f172a;
                    border-bottom: 2px solid #0f172a;
                    color: #1e3a8a;
                    background-color: #f8fafc;
                }
                .footer {
                    margin-top: 30px;
                    padding-top: 16px;
                    border-top: 1px dashed #cbd5e1;
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-end;
                }
                .notes {
                    font-size: 11px;
                    color: #64748b;
                    max-width: 60%;
                }
                .signature-box {
                    text-align: center;
                    border-top: 1px solid #0f172a;
                    padding-top: 6px;
                    font-size: 12px;
                    font-weight: 600;
                    min-width: 160px;
                }
            </style>
        </head>
        <body>
            <div class="receipt-container">
                <div class="header">
                    <div class="institute-title">${settings.name}</div>
                    <div class="institute-tagline">${settings.tagline}</div>
                    <div class="institute-details">${settings.address} | Phone: ${settings.phone}</div>
                    <div><span class="receipt-badge">FEE RECEIPT (PAID)</span></div>
                </div>

                <table class="meta-table">
                    <tr>
                        <td class="label">Receipt No:</td>
                        <td class="val">${payment.receiptNo}</td>
                        <td class="label">Date:</td>
                        <td class="val">${formattedDate}</td>
                    </tr>
                    <tr>
                        <td class="label">Student Name:</td>
                        <td class="val">${payment.studentName}</td>
                        <td class="label">Roll No:</td>
                        <td class="val">${student?.rollNo ?: "—"}</td>
                    </tr>
                    <tr>
                        <td class="label">Class / Course:</td>
                        <td class="val">${payment.gradeClass}</td>
                        <td class="label">Batch:</td>
                        <td class="val">${student?.batch ?: "Regular"}</td>
                    </tr>
                </table>

                <table class="items-table">
                    <thead>
                        <tr>
                            <th>Description</th>
                            <th>Month / Period</th>
                            <th style="text-align:right;">Amount (${settings.currencySymbol})</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Tuition & Coaching Fee</td>
                            <td>${payment.monthYear}</td>
                            <td style="text-align:right;">${settings.currencySymbol} ${(payment.amountPaid + payment.discount).toInt()}</td>
                        </tr>
                        ${if (payment.discount > 0) """
                        <tr>
                            <td style="color:#059669;">Concession / Discount</td>
                            <td>Special Waiver</td>
                            <td style="text-align:right; color:#059669;">- ${settings.currencySymbol} ${payment.discount.toInt()}</td>
                        </tr>
                        """ else ""}
                        <tr class="total-row">
                            <td colspan="2">Net Amount Paid (${payment.paymentMode})</td>
                            <td style="text-align:right;">${settings.currencySymbol} ${payment.amountPaid.toInt()}</td>
                        </tr>
                    </tbody>
                </table>

                <table class="meta-table" style="margin-bottom: 10px;">
                    <tr>
                        <td class="label">Payment Mode:</td>
                        <td class="val">${payment.paymentMode}</td>
                        ${if (payment.transactionId.isNotEmpty()) """
                        <td class="label">Txn / Ref ID:</td>
                        <td class="val">${payment.transactionId}</td>
                        """ else """<td></td><td></td>"""}
                    </tr>
                </table>

                <div class="footer">
                    <div class="notes">
                        * Computer generated receipt. Fee once paid is non-refundable.<br>
                        Thank you for your prompt payment!
                    </div>
                    <div class="signature-box">
                        Authorized Signature<br>
                        <span style="font-size:10px; font-weight:normal; color:#64748b;">${settings.name}</span>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun printReceipt(
        context: Context,
        payment: FeePayment,
        student: Student?,
        settings: InstituteSettings
    ) {
        val webView = WebView(context)
        val html = generateReceiptHtml(payment, student, settings)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter: PrintDocumentAdapter = view.createPrintDocumentAdapter("Receipt_${payment.receiptNo}")
                val jobName = "Receipt_${payment.receiptNo}"
                printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    fun shareReceiptText(
        context: Context,
        payment: FeePayment,
        student: Student?,
        settings: InstituteSettings
    ) {
        val text = buildReceiptText(payment, student, settings)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Fee Receipt - ${payment.receiptNo}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Fee Receipt"))
    }

    fun sendReceiptViaWhatsApp(
        context: Context,
        phone: String,
        payment: FeePayment,
        student: Student?,
        settings: InstituteSettings
    ) {
        val text = buildReceiptText(payment, student, settings)
        sendWhatsAppMessage(context, phone, text)
    }

    fun sendWhatsAppMessage(
        context: Context,
        rawPhone: String,
        message: String
    ) {
        try {
            // Clean phone number: remove non-digits
            var cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
            if (cleanPhone.length == 10) {
                cleanPhone = "91$cleanPhone" // Default India country code if 10 digits
            }
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = if (cleanPhone.isNotEmpty()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed or error opening WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    fun buildReceiptText(
        payment: FeePayment,
        student: Student?,
        settings: InstituteSettings
    ): String {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date(payment.paymentDate))

        return """
        🧾 *FEE RECEIPT — ${settings.name}*
        ━━━━━━━━━━━━━━━━━━━━━
        📋 *Receipt No:* ${payment.receiptNo}
        📅 *Date:* $dateStr
        👤 *Student:* ${payment.studentName}
        📚 *Class:* ${payment.gradeClass}
        🗓️ *Month:* ${payment.monthYear}
        
        💰 *Amount Paid:* ${settings.currencySymbol}${payment.amountPaid.toInt()}
        💳 *Mode:* ${payment.paymentMode} ${if (payment.transactionId.isNotEmpty()) "(${payment.transactionId})" else ""}
        ${if (payment.discount > 0) "🎁 *Discount:* ${settings.currencySymbol}${payment.discount.toInt()}\n" else ""}
        ✅ *Status:* PAID & VERIFIED
        ━━━━━━━━━━━━━━━━━━━━━
        Thank you for choosing *${settings.name}*!
        📞 Contact: ${settings.phone}
        """.trimIndent()
    }
}
