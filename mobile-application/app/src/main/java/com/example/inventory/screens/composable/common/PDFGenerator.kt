package com.example.inventory.screens.composable.common

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.example.inventory.data.SalesResponseDto
import com.example.inventory.data.SaleItemResponseDto
import com.example.inventory.data.BatchResponseDto
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PdfGenerator(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateDailyReport(
        date: LocalDate,
        sales: List<SalesResponseDto>,
        batches: List<BatchResponseDto>,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Create a temporary file first
            val fileName = "daily_sales_report_${date.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))}.pdf"
            val tempFile = File(context.cacheDir, "temp_$fileName")

            val outputStream = FileOutputStream(tempFile)
            val writer = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)

            // Add title
            val title = Paragraph("Daily Sales Report")
                .setFontSize(20f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
            document.add(title)

            // Add date
            val dateParagraph = Paragraph("Date: ${date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}")
                .setFontSize(12f)
                .setMarginBottom(20f)
            document.add(dateParagraph)

            // Calculate financial summary
            val financialSummary = calculateFinancialSummary(sales, batches)

            // Add summary section
            val summaryTable = Table(2)
                .setWidth(UnitValue.createPercentValue(100f))
                .setMarginBottom(20f)

            addSummaryRow(summaryTable, "Total Sales", "$${"%.2f".format(financialSummary.totalSales)}")
            addSummaryRow(summaryTable, "Total Cost", "$${"%.2f".format(financialSummary.totalCost)}")
            addSummaryRow(summaryTable, "Total Profit", "$${"%.2f".format(financialSummary.totalProfit)}")
            addSummaryRow(summaryTable, "Average Profit per Sale", "$${"%.2f".format(financialSummary.averageProfit)}")
            addSummaryRow(summaryTable, "Number of Sales", sales.size.toString())
            addSummaryRow(summaryTable, "Total Items Sold", financialSummary.totalItemsSold.toString())

            document.add(summaryTable)

            // Add sales breakdown
            if (sales.isNotEmpty()) {
                val salesTitle = Paragraph("Sales Breakdown")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginBottom(10f)
                document.add(salesTitle)

                val salesTable = Table(6)
                    .setWidth(UnitValue.createPercentValue(100f))
                    .setMarginBottom(20f)

                // Table headers
                salesTable.addHeaderCell(createHeaderCell("Sale ID"))
                salesTable.addHeaderCell(createHeaderCell("Product"))
                salesTable.addHeaderCell(createHeaderCell("Quantity"))
                salesTable.addHeaderCell(createHeaderCell("Sale Price"))
                salesTable.addHeaderCell(createHeaderCell("Cost Price"))
                salesTable.addHeaderCell(createHeaderCell("Profit"))

                // Table rows
                sales.flatMap { sale ->
                    sale.items.map { saleItem ->
                        SaleItemWithParent(sale.id, saleItem)
                    }
                }.forEach { saleItemWithParent ->
                    val batch = batches.find { it.id == saleItemWithParent.saleItem.batchId }
                    val costPrice = batch?.orderPrice ?: 0.0
                    val profit = (saleItemWithParent.saleItem.price - costPrice) * saleItemWithParent.saleItem.amount

                    salesTable.addCell(createCell(saleItemWithParent.saleId.toString()))
                    salesTable.addCell(createCell(saleItemWithParent.saleItem.productName ?: "Unknown"))
                    salesTable.addCell(createCell(saleItemWithParent.saleItem.amount.toString()))
                    salesTable.addCell(createCell("$${"%.2f".format(saleItemWithParent.saleItem.price)}"))
                    salesTable.addCell(createCell("$${"%.2f".format(costPrice)}"))
                    salesTable.addCell(createCell("$${"%.2f".format(profit)}", profit >= 0))
                }

                document.add(salesTable)
            }

            // Add timestamp
            val timestamp = Paragraph("Generated on: ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"))}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
            document.add(timestamp)

            document.close()

            // Return the temporary file
            onSuccess(tempFile)
        } catch (e: Exception) {
            onError("Failed to generate PDF: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateFinancialSummary(sales: List<SalesResponseDto>, batches: List<BatchResponseDto>): FinancialSummary {
        var totalSales = 0.0
        var totalCost = 0.0
        var totalItemsSold = 0

        sales.flatMap { it.items }.forEach { saleItem ->
            val batch = batches.find { it.id == saleItem.batchId }
            val costPrice = batch?.orderPrice ?: 0.0

            totalSales += saleItem.price * saleItem.amount
            totalCost += costPrice * saleItem.amount
            totalItemsSold += saleItem.amount.toInt()
        }

        val totalProfit = totalSales - totalCost
        val averageProfit = if (sales.isNotEmpty()) totalProfit / sales.size else 0.0

        return FinancialSummary(
            totalSales = totalSales,
            totalCost = totalCost,
            totalProfit = totalProfit,
            averageProfit = averageProfit,
            totalItemsSold = totalItemsSold
        )
    }

    private fun addSummaryRow(table: Table, label: String, value: String) {
        table.addCell(createCell(label).setBold())
        table.addCell(createCell(value))
    }

    private fun createHeaderCell(text: String): Cell {
        return Cell().add(Paragraph(text))
            .setBold()
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
    }

    private fun createCell(text: String, isPositive: Boolean = true): Cell {
        val cell = Cell().add(Paragraph(text))
            .setTextAlignment(TextAlignment.CENTER)

        if (text.startsWith("$") && !isPositive) {
            cell.setFontColor(ColorConstants.RED)
        } else if (text.startsWith("$") && isPositive) {
            cell.setFontColor(ColorConstants.GREEN)
        }

        return cell
    }

    private data class FinancialSummary(
        val totalSales: Double,
        val totalCost: Double,
        val totalProfit: Double,
        val averageProfit: Double,
        val totalItemsSold: Int
    )

    private data class SaleItemWithParent(
        val saleId: Long,
        val saleItem: SaleItemResponseDto
    )
}