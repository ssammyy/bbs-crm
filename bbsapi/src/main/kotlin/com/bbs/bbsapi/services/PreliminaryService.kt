package com.bbs.bbsapi.services

import com.bbs.bbsapi.controllers.ApprovalRequest
import com.bbs.bbsapi.controllers.InitiatePreliminaryRequest
import com.bbs.bbsapi.enums.ApprovalStage
import com.bbs.bbsapi.enums.ApprovalStatus
import com.bbs.bbsapi.enums.PreliminaryStatus
import com.bbs.bbsapi.models.*
import com.bbs.bbsapi.repos.*
import lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Slf4j
class PreliminaryService(
    private val clientRepository: ClientRepo,
    private val preliminaryRepository: PreliminaryRepository,
    private val preliminaryTypeRepository: PreliminaryTypeRepository,
    private val invoiceRepository: InvoiceRepository,
    private val proformaInvoiceRepository: ProformaInvoiceRepository,
    private val approvalRepository: ApprovalRepository,
    private val userService: UserService
) {

    fun initiatePreliminary(clientId: Long, request: InitiatePreliminaryRequest, invoice: Invoice? = null): Preliminary {
        val client = clientRepository.findById(clientId).orElseThrow { IllegalArgumentException("Client not found") }
        println("prelim id  " + request.preliminaryType)
        val preliminaryType = preliminaryTypeRepository.findById(request.preliminaryType)
            .orElseThrow { IllegalArgumentException("Preliminary type not found") }

        val preliminary = Preliminary(
            client = client,
            preliminaryType = preliminaryType,
            status = PreliminaryStatus.INITIATED,
            invoiced = request.invoiced,
            createdAt = LocalDateTime.now(),
            invoice = invoice
        )
        return preliminaryRepository.save(preliminary)
    }


    fun approvePreliminary(preliminaryId: Long, request: ApprovalRequest): Approval {
        val preliminary = preliminaryRepository.findById(preliminaryId)
            .orElseThrow { IllegalArgumentException("Preliminary not found") }

        val approval = Approval(
            preliminary = preliminary,
            approvalStage = request.approvalStage,
            status = ApprovalStatus.APPROVED,
            remarks = request.comments,
            approvedBy = userService.getActualUSer(),
            technicalApproved = request.approvalStage == ApprovalStage.TECHNICAL_DIRECTOR,
            directorApproved = request.approvalStage == ApprovalStage.MANAGING_DIRECTOR,
        )
        val savedApproval = approvalRepository.save(approval)

        // Update preliminary status based on approval
        if (request.status == "APPROVED") {
            val approvals = approvalRepository.findByPreliminaryId(preliminaryId)
            val hasTechnicalDirectorApproval = request.approvalStage == ApprovalStage.TECHNICAL_DIRECTOR
            val hasMDApproval = request.approvalStage == ApprovalStage.MANAGING_DIRECTOR

            preliminary.status = when {
                hasMDApproval -> PreliminaryStatus.COMPLETE
                hasTechnicalDirectorApproval -> PreliminaryStatus.PENDING_M_D_APPROVAL
                else -> PreliminaryStatus.COMPLETE
            }
            preliminaryRepository.save(preliminary)
        }

        return savedApproval
    }


    fun getProformaInvoice(clientId: Long): ProformaInvoice {
        return proformaInvoiceRepository.findByClientId(clientId)
            ?: throw (IllegalArgumentException("Client not found"))
    }


    private fun getTechnicalDirectorId(): Long = 2 // Placeholder: Fetch from user repository
    private fun getMDId(): Long = 3 // Placeholder: Fetch from user repository
    fun createNewPreliminaryType(preliminaryType: PreliminaryType): PreliminaryType {

        println("Preliminary type created " + preliminaryType.name)
        val preliminaryTypeV = PreliminaryType(
            name = preliminaryType.name,
            description = preliminaryType.description,
        )
        return preliminaryTypeRepository.save(preliminaryTypeV)
    }

    fun getClientPreliminaries(clientId: Long): List<Preliminary> {
        val preliminaryList = preliminaryRepository.findByClientId(clientId)
        return preliminaryList

    }

    fun submitTechnicalPreliminary(clientId: Long, preliminary: Preliminary): ResponseEntity<Preliminary> {
        val prelim = preliminaryRepository.findById(preliminary.id)
            .orElseThrow { IllegalArgumentException("Preliminary not found") }
        prelim.status = PreliminaryStatus.PENDING_T_D_APPROVAL
        preliminaryRepository.save(prelim)
        return ResponseEntity.status(HttpStatus.CREATED).body(prelim)

    }

    fun approvePreliminaryInvoice(clientId: Long, preliminaryId: Long): ResponseEntity<Invoice> {
        val invoice = invoiceRepository.findByPreliminaryId(preliminaryId)
        invoice?.directorApproved = true
        invoiceRepository.save(invoice!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice)


    }

    fun getPreliminaryById(preliminaryId: Long): Preliminary {
        return preliminaryRepository.findById(preliminaryId)
            .orElseThrow { IllegalArgumentException("Preliminary not found") }
    }

    fun approvePreliminaryStage(preliminary: Preliminary, approvalStage: ApprovalStage): Preliminary {
        if (approvalStage == ApprovalStage.TECHNICAL_DIRECTOR) {
            preliminary.status = PreliminaryStatus.PENDING_M_D_APPROVAL
        } else {
            preliminary.status = PreliminaryStatus.COMPLETE
        }
        return preliminaryRepository.save(preliminary)
    }

    fun updatePreliminaryStatus(preliminaryId: Long, status: PreliminaryStatus) {
        val preliminary = preliminaryRepository.findById(preliminaryId).orElseThrow()
        preliminary.status = status
        preliminaryRepository.save(preliminary)
    }

}