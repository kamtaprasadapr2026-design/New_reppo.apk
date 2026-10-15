package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.model.Receipt
import java.io.File
import java.io.FileOutputStream

object ReceiptPdfGenerator {

    fun generateA4ReceiptPdf(
        context: Context,
        receipt: Receipt,
        instituteName: String = "Neol Bharat Institute"
    ): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(26, 35, 126)
                textSize = 24f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(66, 66, 66)
                textSize = 14f
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isAntiAlias = true
            }

            val boldPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(200, 200, 200)
                strokeWidth = 1.5f
            }

            var y = 60f
            canvas.drawText(instituteName, 40f, y, titlePaint)
            y += 24f
            canvas.drawText("OFFICIAL PAYMENT RECEIPT", 40f, y, subtitlePaint)
            y += 20f
            canvas.drawLine(40f, y, 555f, y, linePaint)

            y += 35f
            canvas.drawText("Receipt No:", 40f, y, boldPaint)
            canvas.drawText(receipt.receiptNumber.ifBlank { "REC-${receipt.id.take(8)}" }, 160f, y, textPaint)

            y += 25f
            canvas.drawText("Date:", 40f, y, boldPaint)
            canvas.drawText(receipt.paymentDate.ifBlank { "Today" }, 160f, y, textPaint)

            y += 25f
            canvas.drawText("Student Name:", 40f, y, boldPaint)
            canvas.drawText(receipt.studentName, 160f, y, textPaint)

            y += 25f
            canvas.drawText("Student ID:", 40f, y, boldPaint)
            canvas.drawText(receipt.studentId.ifBlank { receipt.userId }, 160f, y, textPaint)

            y += 25f
            canvas.drawText("Email:", 40f, y, boldPaint)
            canvas.drawText(receipt.userEmail, 160f, y, textPaint)

            y += 30f
            canvas.drawLine(40f, y, 555f, y, linePaint)

            y += 35f
            canvas.drawText("Fee Purpose:", 40f, y, boldPaint)
            canvas.drawText("${receipt.purpose} - ${receipt.targetName}", 160f, y, textPaint)

            y += 30f
            canvas.drawText("Amount Paid:", 40f, y, boldPaint)
            val amountText = "₹%.2f".format(receipt.amount)
            val amountPaint = Paint().apply {
                color = Color.rgb(46, 125, 50)
                textSize = 16f
                isFakeBoldText = true
            }
            canvas.drawText(amountText, 160f, y, amountPaint)

            y += 30f
            canvas.drawText("Payment Status:", 40f, y, boldPaint)
            canvas.drawText("VERIFIED & PAID (UPI)", 160f, y, textPaint)

            y += 60f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 25f
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
                isAntiAlias = true
            }
            canvas.drawText("This is a computer-generated receipt issued by Neol Bharat.", 40f, y, footerPaint)

            document.finishPage(page)

            val dir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val file = File(dir, "receipt_${receipt.id.ifBlank { System.currentTimeMillis().toString() }}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
