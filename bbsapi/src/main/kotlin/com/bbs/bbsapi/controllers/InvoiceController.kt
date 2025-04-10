package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.services.InvoiceService
import com.bbs.bbsapi.entities.PdfInvoiceDTO
import com.bbs.bbsapi.entities.InvoiceResponse
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.repos.InvoiceRepository
import com.bbs.bbsapi.services.PdfGenerator
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/invoices")
class InvoiceController(
    private val invoiceService: InvoiceService,
    private val invoiceRepository: InvoiceRepository,
    private val pdfGenerator: PdfGenerator
) {
    @PostMapping( produces = [MediaType.APPLICATION_PDF_VALUE])
    fun createInvoice(@RequestBody request: PdfInvoiceDTO): ResponseEntity<ByteArray> {
        val response = invoiceService.createInvoice(request)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-${response.invoiceNumber}.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(response.pdfContent)
    }
    @GetMapping("/{invoiceId}/pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getInvoicePdf(@PathVariable invoiceId: Long): ResponseEntity<ByteArray> {
        val pdfBytes = invoiceService.getInvoicePdf(invoiceId)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-$invoiceId.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes)
    }

    @PostMapping("/{clientId}/accept")
    fun acceptInvoice(@PathVariable clientId: Long): ResponseEntity<Client> {
        return invoiceService.acceptInvoice(clientId)
    }


}
