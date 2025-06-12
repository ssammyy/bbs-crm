package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.PdfInvoiceDTO
import com.bbs.bbsapi.entities.PaymentConfirmationDTO
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Invoice
import com.bbs.bbsapi.services.InvoiceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/invoices")
class InvoiceController(
    private val invoiceService: InvoiceService
) {

    @PostMapping
    fun createInvoice(@RequestBody request: PdfInvoiceDTO): ResponseEntity<ByteArray> {
        val response = invoiceService.createInvoice(request)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=invoice-${response.invoiceNumber}.pdf")
            .body(response.pdfContent)
    }

    @PostMapping("/balance/{parentInvoiceId}")
    fun createBalanceInvoice(@PathVariable parentInvoiceId: Long): ResponseEntity<ByteArray> {
        val response = invoiceService.createBalanceInvoice(parentInvoiceId)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=invoice-${response.invoiceNumber}.pdf")
            .body(response.pdfContent)
    }

    @GetMapping("/{clientId}/{invoiceType}/pdf")
    fun getInvoicePdf(@PathVariable clientId: Long, @PathVariable invoiceType: InvoiceType, @RequestParam(required = false) govApprovalType: String?): ResponseEntity<ByteArray> {
        val pdfBytes = invoiceService.getInvoicePdf(clientId, invoiceType, govApprovalType)
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=invoice-$clientId.pdf")
            .body(pdfBytes)
    }

    @GetMapping("/{invoiceId}/pdf")
    fun getInvoicePdfById(@PathVariable invoiceId: Long): ResponseEntity<ByteArray> {
        val pdfBytes = invoiceService.getInvoicePdfById(invoiceId)
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=invoice-$invoiceId.pdf")
            .body(pdfBytes)
    }

    @GetMapping("/{clientId}/prelim/{preliminaryId}/pdf")
    fun getPreliminaryInvoicePdf(@PathVariable clientId: Long, @PathVariable preliminaryId: Long): ResponseEntity<ByteArray> {
        val pdfBytes = invoiceService.getPreliminaryInvoicePdf(clientId, preliminaryId)
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=invoice-$clientId.pdf")
            .body(pdfBytes)
    }

    @GetMapping("/proforma/{clientId}")
    fun getProformaInvoice(@PathVariable clientId: Long): ResponseEntity<PdfInvoiceDTO?> {
        val proformaInvoice = invoiceService.getProformaInvoice(clientId)
        return ResponseEntity.ok(proformaInvoice)
    }

    @PostMapping("/{clientId}/accept")
    fun acceptInvoice(
        @PathVariable clientId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Any> {
        val stage = request["stage"]?.let { ClientStage.valueOf(it) }
            ?: return ResponseEntity.badRequest().body("Stage is required")
        val invoiceType = request["invoiceType"]?.let { InvoiceType.valueOf(it) }
            ?: return ResponseEntity.badRequest().body("InvoiceType is required")
        val client = invoiceService.acceptInvoice(clientId, stage, invoiceType)
        return ResponseEntity.ok(client)
    }

    @PostMapping("/{clientId}/reject")
    fun rejectInvoice(
        @PathVariable clientId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Any> {
        val remarks = request["remarks"] ?: return ResponseEntity.badRequest().body("Remarks are required")
        val stage = request["stage"]?.let { ClientStage.valueOf(it) }
            ?: return ResponseEntity.badRequest().body("Stage is required")
        val client = invoiceService.rejectInvoice(clientId, remarks, stage)
        return ResponseEntity.ok(client)
    }

    @GetMapping("/preliminary/{preliminaryId}")
    fun getPreliminaryInvoice(@PathVariable preliminaryId: Long): ResponseEntity<PdfInvoiceDTO?> {
        val invoice = invoiceService.getInvoiceByPreliminaryId(preliminaryId)
        return ResponseEntity.ok(invoice?.let { invoiceService.convertToPdfDto(it) })
    }

    @GetMapping("/all-client-invoices/{clientId}")
    fun getAllInvoices(@PathVariable clientId: Long): ResponseEntity<List<Invoice>> {
        return ResponseEntity.ok(invoiceService.getInvoicesByClientId(clientId))
    }

    @GetMapping("/balance/{parentInvoiceId}")
    fun getBalanceInvoices(@PathVariable parentInvoiceId: Long): ResponseEntity<List<Invoice>> {
        return ResponseEntity.ok(invoiceService.getBalanceInvoicesByParentId(parentInvoiceId))
    }

    @PostMapping("/{invoiceId}/confirm-payment")
    fun confirmPayment(
        @PathVariable invoiceId: Long,
        @RequestBody payment: PaymentConfirmationDTO
    ): ResponseEntity<Invoice> {
        val updatedInvoice = invoiceService.confirmPayment(invoiceId, payment)
        return ResponseEntity.ok(updatedInvoice)
    }
}