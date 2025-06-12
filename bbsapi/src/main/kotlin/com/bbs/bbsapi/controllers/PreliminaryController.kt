package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.InvoiceResponse
import com.bbs.bbsapi.entities.PdfInvoiceDTO
import com.bbs.bbsapi.enums.ApprovalStage
import com.bbs.bbsapi.enums.PreliminaryStatus
import com.bbs.bbsapi.models.*
import com.bbs.bbsapi.repositories.PreliminaryTypeRepository
import com.bbs.bbsapi.services.ClientService
import com.bbs.bbsapi.services.DigitalOceanService
import com.bbs.bbsapi.services.InvoiceService
import com.bbs.bbsapi.services.PreliminaryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/preliminaries")
class PreliminaryController(
    private val preliminaryService: PreliminaryService,
    private val invoiceService: InvoiceService,
    private val preliminaryTypeRepository: PreliminaryTypeRepository,
    private val clientService: ClientService,
    private val digitalOceanService: DigitalOceanService
) {

    @PostMapping("/{clientId}/initiate")
    fun initiatePreliminary(
        @PathVariable clientId: Long,
        @RequestBody request: InitiatePreliminaryRequest
    ): ResponseEntity<Preliminary> {
        val preliminary = preliminaryService.initiatePreliminary(clientId, request)
        return ResponseEntity.ok(preliminary)
    }

    @GetMapping("/{clientId}")
    fun getPreliminaries(@PathVariable clientId: Long): ResponseEntity<List<Preliminary>> {
        val prelims = preliminaryService.getClientPreliminaries(clientId)
        return ResponseEntity.ok(prelims)
    }

    @PostMapping("/create")
    fun createPreliminaryType(@RequestBody preliminaryType: PreliminaryType): ResponseEntity<PreliminaryType> {
        val newPreliminary = preliminaryService.createNewPreliminaryType(preliminaryType)
        return ResponseEntity.ok(newPreliminary)
    }

    @PostMapping("/{preliminaryId}/invoice")
    fun generateInvoice(
        @PathVariable preliminaryId: Long,
        @RequestBody request: PdfInvoiceDTO
    ): ResponseEntity<InvoiceResponse> {
        val invoice = invoiceService.createInvoice(request)
        return ResponseEntity.ok(invoice)
    }

    @PostMapping("/{preliminaryId}/approve")
    fun approvePreliminary(
        @PathVariable preliminaryId: Long,
        @RequestBody request: ApprovalRequest
    ): ResponseEntity<Approval> {
        val approval = preliminaryService.approvePreliminary(preliminaryId, request)
        return ResponseEntity.ok(approval)
    }

    @PostMapping("/{preliminaryId}/reject")
    fun rejectPreliminary(
        @PathVariable preliminaryId: Long,
        @RequestBody request: ApprovalRequest
    ): ResponseEntity<Approval> {
        val approval = preliminaryService.rejectPreliminary(preliminaryId, request)
        return ResponseEntity.ok(approval)
    }

    @PostMapping("/{clientId}/technical-works")
    fun submitTechnicalPreliminary(
        @PathVariable clientId: Long,
        @RequestBody preliminary: Preliminary
    ): ResponseEntity<Preliminary> {
        return preliminaryService.submitTechnicalPreliminary(clientId, preliminary)
    }

    @PostMapping("/{clientId}/approve-invoice/{preliminaryId}")
    fun approvePreliminaryInvoice(
        @PathVariable clientId: Long,
        @PathVariable preliminaryId: Long,
    ): ResponseEntity<Invoice> {
        return preliminaryService.approvePreliminaryInvoice(clientId, preliminaryId)
    }
    @PostMapping("/{clientId}/approve-county-invoice/{invoiceType}")
    fun approveCountyInvoice(
        @PathVariable clientId: Long,
        @PathVariable invoiceType: String,
    ): ResponseEntity<Invoice> {
        return preliminaryService.approveCountyInvoice(clientId, invoiceType)
    }



    @GetMapping("/files/{clientId}/{preliminaryId}")
    fun getFiles(
        @PathVariable clientId: Long,
        @PathVariable preliminaryId: Long
    ): ResponseEntity<List<Map<String, Any?>>> {
        val client = clientService.getClientById(clientId)
        val prelim = preliminaryService.getPreliminaryById(preliminaryId)
        return digitalOceanService.getPreliminaryFiles(client, prelim)
    }

    @PostMapping("/approve/{approvalStage}")
    fun approvePreliminaryStage(
        @PathVariable approvalStage: ApprovalStage,
        @RequestBody preliminary: Preliminary
    ): ResponseEntity<Preliminary> {
        val prelim = preliminaryService.approvePreliminaryStage(preliminary, approvalStage)
        return ResponseEntity.ok(prelim)
    }

    @PostMapping("/reject/{approvalStage}")
    fun rejectPreliminaryStage(
        @PathVariable approvalStage: ApprovalStage,
        @RequestBody request: RejectionRequest
    ): ResponseEntity<Preliminary> {
        val prelim = preliminaryService.rejectPreliminary(request.preliminary.id, ApprovalRequest(approvalStage, "REJECTED", request.remarks))
        return ResponseEntity.ok(prelim.preliminary)
    }

    @GetMapping("/{clientId}/proforma")
    fun getProformaInvoice(@PathVariable clientId: Long): ResponseEntity<ProformaInvoice> {
        val proforma = preliminaryService.getProformaInvoice(clientId)
        return ResponseEntity.ok(proforma)
    }

    @GetMapping("/types")
    fun getPreliminaryTypes(): ResponseEntity<List<PreliminaryType>> {
        return ResponseEntity.ok(preliminaryTypeRepository.findAll())
    }

    @PostMapping("/bypass-invoice")
    fun bypassInvoiceClearance(
        @RequestBody preliminary: Preliminary
    ): ResponseEntity<Preliminary> {
        preliminaryService.updatePreliminaryStatus(preliminary.id, PreliminaryStatus.COMPLETE)
        return ResponseEntity.ok(preliminary)
    }
}

data class InitiatePreliminaryRequest(
    val preliminaryType: Long,
    val invoiced: Boolean = false
)
data class InvoiceRequest(val amount: BigDecimal)
data class ApprovalRequest(val approvalStage: ApprovalStage, val status: String, val comments: String?)
data class RejectionRequest(val preliminary: Preliminary, val remarks: String?)