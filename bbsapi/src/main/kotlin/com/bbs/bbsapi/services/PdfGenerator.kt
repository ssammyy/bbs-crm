package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.PdfInvoiceDTO
import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.Image
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import com.lowagie.text.pdf.CMYKColor
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

@Component
class PdfGenerator {

    private val companyFont = Font(Font.HELVETICA, 10f, Font.NORMAL, CMYKColor.BLACK) // Adjusted font size
    private val companyBoldFont =
        Font(Font.HELVETICA, 10f, Font.BOLD, CMYKColor.BLACK) // New bold font for company name
    private val normalFont = Font(Font.HELVETICA, 8f, Font.NORMAL, CMYKColor.BLACK) // Adjusted font size
    private val titleFont = Font(Font.HELVETICA, 16f, Font.BOLD, CMYKColor.BLACK)
    private val logoResource = ClassPathResource("static/logo.jpeg") // Assuming your logo is named bbs_logo.jpeg

    fun generateInvoicePdf(invoice: PdfInvoiceDTO): ByteArray {
        val document = Document(PageSize.A4)
        val out = ByteArrayOutputStream()
        PdfWriter.getInstance(document, out)
        document.open()

        // Add company logo
        val logo = Image.getInstance(logoResource.inputStream.readBytes())
        logo.scaleToFit(100f, 100f)
        logo.alignment = Element.ALIGN_RIGHT
        document.add(logo)

        // Add company name
        val companyName = Paragraph("BBS CONSTRUCTION", companyBoldFont)
        companyName.alignment = Element.ALIGN_RIGHT
        document.add(companyName)

        // Add invoice title
        val title = Paragraph("INVOICE", titleFont)
        title.alignment = Element.ALIGN_CENTER
        title.spacingAfter = 20f
        document.add(title)

        // Add invoice details
        val detailsTable = PdfPTable(2)
        detailsTable.widthPercentage = 100f
        detailsTable.setWidths(floatArrayOf(1f, 1f))

        // Left column
        detailsTable.addCell(createCell("Invoice Number: ${invoice.invoiceNumber}", normalFont))
        detailsTable.addCell(createCell("Date: ${invoice.dateIssued}", normalFont))
        detailsTable.addCell(createCell("Client Name: ${invoice.clientName}", normalFont))
        detailsTable.addCell(createCell("Phone: ${invoice.clientPhone}", normalFont))
        detailsTable.addCell(createCell("Project: ${invoice.projectName}", normalFont))
        document.add(detailsTable)

        // Add items table
        val itemsTable = PdfPTable(4)
        itemsTable.widthPercentage = 100f
        itemsTable.setWidths(floatArrayOf(3f, 1f, 1f, 1f))

        // Add table headers
        itemsTable.addCell(createCell("Description", companyBoldFont))
        itemsTable.addCell(createCell("Quantity", companyBoldFont))
        itemsTable.addCell(createCell("Unit Price", companyBoldFont))
        itemsTable.addCell(createCell("Total", companyBoldFont))

        // Add items
        invoice.items.forEach { item ->
            itemsTable.addCell(createCell(item.description, normalFont))
            itemsTable.addCell(createCell(item.quantity.toString(), normalFont))
            itemsTable.addCell(createCell(String.format("%.2f", item.unitPrice), normalFont))
            itemsTable.addCell(createCell(String.format("%.2f", item.totalPrice), normalFont))
        }

        document.add(itemsTable)

        // Add totals
        val totalsTable = PdfPTable(2)
        totalsTable.widthPercentage = 50f
        totalsTable.horizontalAlignment = Element.ALIGN_RIGHT
        totalsTable.setWidths(floatArrayOf(1f, 1f))

        // Add subtotal
        totalsTable.addCell(createCell("Subtotal:", companyBoldFont))
        totalsTable.addCell(createCell(String.format("%.2f", invoice.subtotal), normalFont))

        // Add discount if applicable
        if (invoice.discountPercentage > 0 || invoice.discountAmount > 0) {
            if (invoice.discountPercentage > 0) {
                totalsTable.addCell(createCell("Discount (${invoice.discountPercentage}%):", companyBoldFont))
            } else {
                totalsTable.addCell(createCell("Discount:", companyBoldFont))
            }
            totalsTable.addCell(createCell(String.format("%.2f", invoice.discountAmount), normalFont))
        }

        // Add final total
        totalsTable.addCell(createCell("Total:", companyBoldFont))
        totalsTable.addCell(createCell(String.format("%.2f", invoice.finalTotal), companyBoldFont))

        document.add(totalsTable)

        document.close()
        return out.toByteArray()
    }

    private fun createCell(text: String, font: Font): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.border = Rectangle.NO_BORDER
        cell.setPadding(5f)
        return cell
    }

    private fun convertToWords(number: Int): String {
        val units = arrayOf("", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
        val teens = arrayOf(
            "ten",
            "eleven",
            "twelve",
            "thirteen",
            "fourteen",
            "fifteen",
            "sixteen",
            "seventeen",
            "eighteen",
            "nineteen"
        )
        val tens = arrayOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")

        return when {
            number == 0 -> "zero"
            number < 10 -> units[number]
            number < 20 -> teens[number - 10]
            number < 100 -> tens[number / 10] + " " + units[number % 10]
            number < 1000 -> units[number / 100] + " hundred " + convertToWords(number % 100)
            number < 1000000 -> convertToWords(number / 1000) + " thousand " + convertToWords(number % 1000)
            else -> number.toString()
        }.trim().replace("\\s+".toRegex(), " ")
    }
}