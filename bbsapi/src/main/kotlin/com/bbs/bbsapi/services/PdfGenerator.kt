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
    private val companyFont = Font(Font.HELVETICA, 10f, Font.NORMAL, CMYKColor.BLACK)
    private val companyBoldFont = Font(Font.HELVETICA, 10f, Font.BOLD, CMYKColor.BLACK)
    private val normalFont = Font(Font.HELVETICA, 8f, Font.NORMAL, CMYKColor.BLACK)
    private val titleFont = Font(Font.HELVETICA, 16f, Font.BOLD, CMYKColor.BLACK)
    private val logoResource = ClassPathResource("static/Colored.png")
    val log = org.slf4j.LoggerFactory.getLogger(PdfGenerator::class.java)
    fun generateInvoicePdf(invoice: PdfInvoiceDTO): ByteArray {
        log.info("Generating invoice pdf for invoice number: ${invoice.invoiceNumber}")
        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 36f, 36f, 36f, 36f)
        val writer = PdfWriter.getInstance(document, outputStream)

        document.open()

        // Header Table with Logo and Company Details
        val headerTable = PdfPTable(2).apply {
            widthPercentage = 100f
            setWidths(floatArrayOf(0.4f, 0.6f))
            defaultCell.border = Rectangle.NO_BORDER

            val logoCell = PdfPCell().apply {
                border = Rectangle.NO_BORDER
                horizontalAlignment = Element.ALIGN_LEFT
                verticalAlignment = Element.ALIGN_MIDDLE
                try {
                    val imageUrl = logoResource.url
                    val image = Image.getInstance(imageUrl)
                    image.scaleToFit(120f, 60f)
                    addElement(image)
                } catch (e: Exception) {
                    e.printStackTrace()
                    addElement(Paragraph("Logo", normalFont))
                }
            }
            addCell(logoCell)

            val companyDetails = PdfPTable(1).apply {
                defaultCell.border = Rectangle.NO_BORDER
                defaultCell.horizontalAlignment = Element.ALIGN_RIGHT
                addCell(Phrase("BENCHMARK", companyBoldFont))
                addCell(Phrase("Building Solutions Ltd", companyFont))
                addCell(Phrase("K-unity Building, Basement, Biashara street, Kiambu", normalFont))
                addCell(Phrase("P.O.BOX 8213 - 00200", normalFont))
                addCell(Phrase("NAIROBI", normalFont))
                addCell(Phrase("0722 333 324", normalFont))
            }
            val companyCell = PdfPCell(companyDetails).apply {
                border = Rectangle.NO_BORDER
                verticalAlignment = Element.ALIGN_MIDDLE
            }
            addCell(companyCell)

            setSpacingAfter(15f)
        }
        document.add(headerTable)

        // Invoice Title
        val title = Paragraph("INVOICE", titleFont).apply {
            alignment = Element.ALIGN_LEFT
            spacingBefore = 10f
            spacingAfter = 15f
        }
        document.add(title)

        // Details Table
        val detailsTable = PdfPTable(4).apply {
            widthPercentage = 100f
            setWidths(floatArrayOf(0.5f, 1.5f, 0.5f, 1.5f))
            setSpacingAfter(20f)
            defaultCell.border = Rectangle.NO_BORDER

            addCell(Phrase("Submitted on:", companyBoldFont))
            addCell(Phrase(invoice.dateIssued.format(DateTimeFormatter.ofPattern("d'th' MMMM yyyy")), normalFont))
            addCell(Phrase("Due Date:", companyBoldFont))
            addCell(Phrase(invoice.dateIssued.plusDays(7).format(DateTimeFormatter.ofPattern("d'th' MMMM yyyy")), normalFont))

            addCell(Phrase("Invoice for", companyBoldFont))
            addCell(Phrase(invoice.clientName, normalFont))
            addCell(Phrase("Project", companyBoldFont))
            addCell(Phrase(invoice.projectName, normalFont))

            addCell(Phrase(" ", normalFont))
            addCell(Phrase(invoice.clientPhone, normalFont))
            addCell(Phrase("Invoice #", companyBoldFont))
            addCell(Phrase(invoice.invoiceNumber, normalFont))

            if (invoice.parentInvoiceId != null) {
                addCell(Phrase("Parent Invoice #", companyBoldFont))
                addCell(Phrase("Parent-${invoice.parentInvoiceId}", normalFont))
                addCell(Phrase("", normalFont))
                addCell(Phrase("", normalFont))
            }
        }
        document.add(detailsTable)

        // Payment Details Table
        val paymentTable = PdfPTable(2).apply {
            widthPercentage = 100f
            setWidths(floatArrayOf(0.5f, 1.5f))
            setSpacingBefore(15f)
            setSpacingAfter(20f)
            defaultCell.border = Rectangle.NO_BORDER

            addCell(Phrase("Payable to", companyBoldFont))
            addCell(Phrase("Bank: NCBA\nBranch: Kiambu\nA/C Number: 5810980017\nA/C Name: Benchmark Building Solutions LTD", normalFont))
            addCell(Phrase("NCBA PAYBILL NO", companyBoldFont))
            addCell(Phrase("880100", normalFont))
            addCell(Phrase("SEND MONEY", companyBoldFont))
            addCell(Phrase("0741 817007", normalFont))
        }
        document.add(paymentTable)

        // Items Table
        val itemsTable = PdfPTable(4).apply {
            widthPercentage = 100f
            setWidths(floatArrayOf(4f, 1f, 2f, 2f))
            headerRows = 1
            setSpacingBefore(10f)
            setSpacingAfter(20f)

            addCell(createCell("Description", companyBoldFont, true))
            addCell(createCell("Qty", companyBoldFont, true))
            addCell(createCell("Unit price(kshs)", companyBoldFont, true))
            addCell(createCell("Total price(kshs)", companyBoldFont, true))

            invoice.items.forEach {
                addCell(createCell(formatDescription(it.description), normalFont))
                addCell(createCell(it.quantity.toString(), normalFont))
                addCell(createCell(formatAmount(it.unitPrice), normalFont))
                addCell(createCell(formatAmount(it.totalPrice), normalFont))
            }
        }
        document.add(itemsTable)

        // Total Section
        val totalTable = PdfPTable(2).apply {
            widthPercentage = 40f
            setWidths(floatArrayOf(2f, 2f))
            horizontalAlignment = Element.ALIGN_RIGHT
            setSpacingBefore(10f)

            addCell(createCell("Subtotal", normalFont, horizontalAlignment = Element.ALIGN_RIGHT))
            addCell(createCell("kshs ${formatAmount(invoice.subtotal)}", normalFont))

            if (invoice.discountPercentage > 0 || invoice.discountAmount > 0) {
                if (invoice.discountPercentage > 0) {
                    addCell(createCell("Discount (${invoice.discountPercentage}%)", normalFont, horizontalAlignment = Element.ALIGN_RIGHT))
                } else {
                    addCell(createCell("Discount", normalFont, horizontalAlignment = Element.ALIGN_RIGHT))
                }
                addCell(createCell("kshs ${formatAmount(invoice.discountAmount)}", normalFont))
            }

            addCell(createCell("TOTAL", companyBoldFont, horizontalAlignment = Element.ALIGN_RIGHT))
            addCell(createCell("kshs ${formatAmount(invoice.finalTotal)}", companyBoldFont))
        }
        document.add(totalTable)

        // Total in Words
        val totalWords = Paragraph("${convertToWords(invoice.finalTotal.toInt())} Kenya shillings only.", normalFont).apply {
            alignment = Element.ALIGN_RIGHT
            spacingBefore = 5f
        }
        document.add(totalWords)

        // Notes Section
        if (!invoice.notes.isNullOrBlank()) {
            log.info("Adding notes to invoice ${invoice.invoiceNumber}")
            val notesTitle = Paragraph("Notes:", companyBoldFont).apply {
                alignment = Element.ALIGN_LEFT
                spacingBefore = 10f
            }
            document.add(notesTitle)
            val notesBody = Paragraph(invoice.notes, normalFont).apply {
                alignment = Element.ALIGN_LEFT
                spacingBefore = 2f
            }
            document.add(notesBody)
        }else
            log.info("No notes provided for invoice ${invoice.invoiceNumber}")

        document.close()
        writer.close()

        return outputStream.toByteArray()
    }

    private fun createCell(
        text: String,
        font: Font,
        isHeader: Boolean = false,
        horizontalAlignment: Int = Element.ALIGN_LEFT
    ): PdfPCell {
        return PdfPCell(Phrase(text, font)).apply {
            setPadding(5f)
            borderWidth = if (isHeader) 1f else 0.5f
            backgroundColor = if (isHeader) CMYKColor.LIGHT_GRAY else CMYKColor.WHITE
            setHorizontalAlignment(
                if (isHeader && horizontalAlignment == Element.ALIGN_LEFT) Element.ALIGN_CENTER else horizontalAlignment
            )
        }
    }

    private fun convertToWords(number: Int): String {
        val units = arrayOf("", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
        val teens = arrayOf(
            "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
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

    // Add helper for formatting description
    private fun formatDescription(description: String): String {
        return description.replace("_", " ")
            .lowercase()
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            .trim()
    }

    // Add helper for formatting amounts with commas
    private fun formatAmount(amount: Double): String {
        return "%,.2f".format(amount)
    }
}