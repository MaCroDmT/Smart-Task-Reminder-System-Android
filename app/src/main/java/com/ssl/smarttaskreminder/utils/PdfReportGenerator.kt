package com.ssl.smarttaskreminder.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.ssl.smarttaskreminder.SessionManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility to generate a professional PDF report for Sonia and Sweaters Limited.
 */
object PdfReportGenerator {

    fun generateWeeklyReport(
        context: Context,
        pending: Int,
        overdue: Int,
        completed: Int,
        completionPct: Float,
        bestPerformers: List<Pair<String, Int>>,
        worstPerformers: List<Pair<String, Int>>,
        deptHealthScores: Map<String, Float>
    ): File? {
        val fileName = "Weekly_Task_Report_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            // Header
            document.add(Paragraph("SONIA AND SWEATERS LIMITED")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY))

            document.add(Paragraph("WEEKLY PERFORMANCE REPORT")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14f)
                .setBold()
                .setFontColor(ColorConstants.GRAY))

            document.add(Paragraph("Generated on: $dateStr")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10f))

            document.add(Paragraph("\nCompany: ${SessionManager.companyName}")
                .setBold())

            // Executive Summary Section
            document.add(Paragraph("\nExecutive Summary")
                .setBold()
                .setFontSize(12f)
                .setUnderline())

            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
            summaryTable.addCell(createLabelCell("Total Completion Rate:"))
            summaryTable.addCell(createValueCell("%.1f%%".format(completionPct)))
            summaryTable.addCell(createLabelCell("Completed Tasks:"))
            summaryTable.addCell(createValueCell(completed.toString()))
            summaryTable.addCell(createLabelCell("Pending Tasks:"))
            summaryTable.addCell(createValueCell(pending.toString()))
            summaryTable.addCell(createLabelCell("Overdue Tasks:"))
            summaryTable.addCell(createValueCell(overdue.toString()))
            document.add(summaryTable)

            // Dept Health Section
            document.add(Paragraph("\nDepartmental Health Scores (Bottleneck Analysis)")
                .setBold()
                .setFontSize(12f)
                .setUnderline())

            if (deptHealthScores.isEmpty()) {
                document.add(Paragraph("No departmental data available."))
            } else {
                val healthTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f))).useAllAvailableWidth()
                healthTable.addCell(createHeaderCell("Department Name"))
                healthTable.addCell(createHeaderCell("Health Score (%)"))
                deptHealthScores.forEach { (dept, score) ->
                    healthTable.addCell(createValueCell(dept))
                    healthTable.addCell(createValueCell("%.1f%%".format(score)))
                }
                document.add(healthTable)
            }

            // Top Performers Section
            document.add(Paragraph("\nTop Performers (On-Time Completion)")
                .setBold()
                .setFontSize(12f)
                .setUnderline())

            if (bestPerformers.isEmpty()) {
                document.add(Paragraph("No performance data available."))
            } else {
                val bestTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f))).useAllAvailableWidth()
                bestTable.addCell(createHeaderCell("Employee Name"))
                bestTable.addCell(createHeaderCell("On-Time Tasks"))
                bestPerformers.forEach { (name, count) ->
                    bestTable.addCell(createValueCell(name))
                    bestTable.addCell(createValueCell(count.toString()))
                }
                document.add(bestTable)
            }

            // Areas for Improvement Section
            document.add(Paragraph("\nAreas for Improvement (Overdue/Late)")
                .setBold()
                .setFontSize(12f)
                .setUnderline())

            if (worstPerformers.isEmpty()) {
                document.add(Paragraph("No critical issues found."))
            } else {
                val worstTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f))).useAllAvailableWidth()
                worstTable.addCell(createHeaderCell("Employee Name"))
                worstTable.addCell(createHeaderCell("Late Tasks"))
                worstPerformers.forEach { (name, count) ->
                    worstTable.addCell(createValueCell(name))
                    worstTable.addCell(createValueCell(count.toString()))
                }
                document.add(worstTable)
            }

            // Footer
            document.add(Paragraph("\n\n\n\n"))
            document.add(Paragraph("---------------------------------------")
                .setTextAlignment(TextAlignment.CENTER))
            document.add(Paragraph("Designed & Developed By Software Engineering Dept.\nSonia and Sweaters Limited — © Prottoy Saha")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8f)
                .setFontColor(ColorConstants.GRAY))

            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun createLabelCell(text: String): Cell {
        return Cell().add(Paragraph(text).setBold().setFontSize(10f))
            .setPadding(5f)
    }

    private fun createValueCell(text: String): Cell {
        return Cell().add(Paragraph(text).setFontSize(10f))
            .setPadding(5f)
    }

    private fun createHeaderCell(text: String): Cell {
        return Cell().add(Paragraph(text).setBold().setFontSize(10f))
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setPadding(5f)
    }

    fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}
