package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.AcceptInvoiceRequest
import com.bbs.bbsapi.services.InvoiceService
import com.bbs.bbsapi.entities.PdfInvoiceDTO
import com.bbs.bbsapi.entities.InvoiceResponse
import com.bbs.bbsapi.entities.RejectInvoiceRequest
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.repos.InvoiceRepository
import com.bbs.bbsapi.services.PdfGenerator
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
    fun acceptInvoice(
        @PathVariable clientId: Long,
        @RequestBody request: AcceptInvoiceRequest
    ): ResponseEntity<*> {
        try {
            val client =
                request.invoiceType?.let {
                    invoiceService.acceptInvoice(clientId, ClientStage.valueOf(request.stage),
                        it
                    )
                }
            return ResponseEntity.ok(client)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: IllegalStateException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @PostMapping("/{clientId}/reject")
    fun rejectInvoice(
        @PathVariable clientId: Long,
        @RequestBody request: RejectInvoiceRequest
    ): ResponseEntity<Client> {
        try {
            val client = invoiceService.rejectInvoice(clientId, request.remarks, ClientStage.valueOf(request.stage))
            return ResponseEntity.ok(client)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        } catch (e: IllegalStateException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }


}
