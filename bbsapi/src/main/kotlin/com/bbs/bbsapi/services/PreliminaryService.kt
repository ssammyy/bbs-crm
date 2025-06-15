package com.bbs.bbsapi.services

import com.bbs.bbsapi.controllers.ApprovalRequest
import com.bbs.bbsapi.controllers.InitiatePreliminaryRequest
import com.bbs.bbsapi.enums.*
import com.bbs.bbsapi.enums.ApprovalStatus
import com.bbs.bbsapi.models.*
import com.bbs.bbsapi.repositories.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Slf4j
class PreliminaryService(
    private val clientRepository: ClientRepository,
    private val preliminaryRepository: PreliminaryRepository,
    private val preliminaryTypeRepository: PreliminaryTypeRepository,
    private val invoiceRepository: InvoiceRepository,
    private val proformaInvoiceRepository: ProformaInvoiceRepository,
    private val approvalRepository: ApprovalRepository,
    private val userService: UserService,
    private val fileRepository: FileRepository,
    private val emailService: EmailService,
    private val invoiceService: InvoiceService,
    private val digitalOceanService: DigitalOceanService,
    private val clientService: ClientService,
    private val userRepository: UserRepository,
) {
    val log = LoggerFactory.getLogger(PreliminaryService::class.java)

    fun initiatePreliminary(
        clientId: Long,
        request: InitiatePreliminaryRequest,
        invoice: Invoice? = null
    ): Preliminary {
        val client = clientRepository.findById(clientId).orElseThrow { IllegalArgumentException("Client not found") }
        val alreadyExists = preliminaryRepository.findByClientIdAndPreliminaryType_Id(clientId, request.preliminaryType)
        if (alreadyExists != null) {
            throw IllegalStateException("A preliminary of that type already exists for this context.")
        }

        val preliminaryType = preliminaryTypeRepository.findById(request.preliminaryType)
            .orElseThrow { IllegalArgumentException("Preliminary type not found") }

        val preliminary = Preliminary(
            client = client,
            preliminaryType = preliminaryType,
            status = PreliminaryStatus.INITIATED,
            invoiced = request.invoiced,
            createdAt = LocalDateTime.now(),
            invoice = invoice,
            rejectionRemarks = null
        )
//        if preliminary type = x, then roleName =Y, else roleName = U
        val role = if (preliminaryType.name ==="BOQ_PREPARATION" ) RoleEnum.QUALITY_SURVEYOR.toString() else RoleEnum.ARCHITECT.toString()

//        send email to either QS/Architect
        CoroutineScope(Dispatchers.IO).launch {
            emailService.sendEmail(
                "samuikumbu@gmail.com",
                "${preliminary.preliminaryType?.name} PENDING YOUR APPROVAL",
                "Dear user, \nA ${preliminary.preliminaryType?.name} preliminary for ${preliminary.client?.firstName} client has been initialised, pending your action",
                userRepository.findFirstByRole_Name(role).get()

//                attachment = attachment
            )
        }

        return preliminaryRepository.save(preliminary)
    }

    /**
     * If preliminary requires government approval, then it should follow a flow after MD has approved
     * First check whether all documents are uploaded, if not request for all document upload, including valid practicing licences for architects and engineers and valid NCA Licences
     * documents are then submitted to the county government.
     * the county government issues an invoice which should then be uploaded to the system,
     *
     *
     */

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

        if (request.status == "APPROVED") {
            val hasTechnicalDirectorApproval = request.approvalStage == ApprovalStage.TECHNICAL_DIRECTOR
            val hasMDApproval = request.approvalStage == ApprovalStage.MANAGING_DIRECTOR
            val pendingGovApprovals = preliminary.preliminaryType?.requiresGovernmentApproval
            log.info("pending goovernment approval $pendingGovApprovals ")


            preliminary.status = when {
                pendingGovApprovals == true && hasMDApproval -> PreliminaryStatus.PENDING_SUBMISSION_OF_FILES_TO_COUNTY
                hasMDApproval && !pendingGovApprovals!! -> PreliminaryStatus.COMPLETE
                hasTechnicalDirectorApproval -> PreliminaryStatus.PENDING_M_D_APPROVAL
                else -> PreliminaryStatus.COMPLETE
            }
            preliminary.rejectionRemarks = null
            preliminaryRepository.save(preliminary)
        }

        return savedApproval
    }

    fun rejectPreliminary(preliminaryId: Long, request: ApprovalRequest): Approval {
        val preliminary = preliminaryRepository.findById(preliminaryId)
            .orElseThrow { IllegalArgumentException("Preliminary not found") }

        val approval = Approval(
            preliminary = preliminary,
            approvalStage = request.approvalStage,
            status = ApprovalStatus.REJECTED,
            remarks = request.comments,
            approvedBy = userService.getActualUSer(),
            technicalApproved = false,
            directorApproved = false,
        )
        val savedApproval = approvalRepository.save(approval)

        preliminary.status = PreliminaryStatus.INITIATED
        preliminary.rejectionRemarks = request.comments
        preliminaryRepository.save(preliminary)

        return savedApproval
    }

    fun getProformaInvoice(clientId: Long): ProformaInvoice {
        return proformaInvoiceRepository.findByClientId(clientId)
            ?: throw (IllegalArgumentException("Client not found"))
    }

    private fun getTechnicalDirectorId(): Long = 2
    private fun getMDId(): Long = 3
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
//        val attachments = digitalOceanService.getPreliminaryFiles(clientService.getClientById(clientId), preliminary)
//        val attachment = EmailService.EmailAttachment(
//            fileName = "site_visit_invoice.pdf",
//            content = attachments,
//            contentType = "application/pdf"
//        )
//       send email to TD

        CoroutineScope(Dispatchers.IO).launch {
            emailService.sendEmail(
                "samuikumbu@gmail.com",
                "${preliminary.preliminaryType?.name} PENDING YOUR APPROVAL",
                "Dear user, \nFind the document attached below and log in to the system to action on ${preliminary.preliminaryType?.name} for ${preliminary.client?.firstName} client. ",
                userRepository.findFirstByRole_Name(RoleEnum.TECHNICAL_DIRECTOR.toString()).get()

//                attachment = attachment
            )
        }
        val authentication = SecurityContextHolder.getContext().authentication


        clientService.addClientActivity(
            clientService.getClientById(clientId),
            "${authentication.name} uploaded their ${preliminary.preliminaryType?.name} work for ${preliminary.client?.firstName}",
        )

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


    @Transactional
    fun approveCountyInvoice(clientId: Long, type: String): ResponseEntity<Invoice> {
        val invoice = invoiceRepository.findByClientIdAndGovernmentApprovalType(clientId, type)
        invoice?.directorApproved = true
        invoiceRepository.save(invoice!!)

        val preliminary= preliminaryRepository.findByClientIdAndPreliminaryType_Name(clientId, type)
            ?: throw (IllegalArgumentException("preliminary not found"))
        preliminary.status = PreliminaryStatus.PENDING_APPROVAL_BY_COUNTY
        preliminaryRepository.save(preliminary)

        return ResponseEntity.status(HttpStatus.CREATED).body(invoice)
    }
    @Transactional
    fun approveMainProforma(clientId: Long): ResponseEntity<Invoice> {
        val invoice = invoiceRepository.findByClientIdAndInvoiceType(clientId, InvoiceType.MAIN_PROFORMA)
        invoice?.directorApproved = true
        invoiceRepository.save(invoice!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice)
    }


    fun getPreliminaryById(preliminaryId: Long): Preliminary {
        return preliminaryRepository.findById(preliminaryId)
            .orElseThrow { IllegalArgumentException("Preliminary not found") }
    }

    fun approvePreliminaryStage(preliminary: Preliminary, approvalStage: ApprovalStage): Preliminary {
        log.info("logging preliminary type ", preliminary.preliminaryType?.name)

        val authentication = SecurityContextHolder.getContext().authentication

        preliminary?.client?.let {
            clientService.addClientActivity(
                it,
                "${authentication.name} approved ${preliminary.preliminaryType?.name} work for ${preliminary.client?.firstName}",
            )
        }
        if (approvalStage == ApprovalStage.TECHNICAL_DIRECTOR) {
//            send email to MD
//            val invoicePdf = getInvoicePdf(client.id, InvoiceType.SITE_VISIT)


            CoroutineScope(Dispatchers.IO).launch {
                emailService.sendEmail(
                    "samuikumbu@gmail.com",
                    "${preliminary.preliminaryType?.name} PENDING YOUR APPROVAL",
                    "Dear user, \nFind the document attached below and log in to the system to action on ${preliminary.preliminaryType?.name} for ${preliminary.client?.firstName} client. ",
                    userRepository.findFirstByRole_Name(RoleEnum.MANAGING_DIRECTOR.toString()).get()
//                    attachment = attachment
                )
            }
            preliminary.status = PreliminaryStatus.PENDING_M_D_APPROVAL
            preliminary.rejectionRemarks = null
        }else if(approvalStage == ApprovalStage.SUBMITTED_TO_COUNTY) {
            preliminary.status = PreliminaryStatus.PENDING_COUNTY_FEE_PAYMENT
            CoroutineScope(Dispatchers.IO).launch {
                emailService.sendEmail(
                    "samuikumbu@gmail.com",
                    " ${preliminary.preliminaryType?.name} PENDING COUNTY APPROVAL",
                    "Dear ${preliminary.client?.firstName}, \nYour documents have been submitted to the county government for approval, in case of any changes, we will keep you posted. \n Regards ",
                    userRepository.findByEmail(preliminary.client?.email!!)
//                    attachment = attachment
                )
            }

        }else {
            val pendingGovApprovals = preliminary.preliminaryType?.requiresGovernmentApproval

            val file = fileRepository.findByClientAndPreliminary(preliminary.client!!, preliminary)
            for (fi in file) {
                fi.approved = true
                fileRepository.save(fi)
            }
            preliminary.status = when{
                pendingGovApprovals == true -> PreliminaryStatus.PENDING_SUBMISSION_OF_FILES_TO_COUNTY
                else -> {
                    log.info("iko hapa >>")
                    val prelim = preliminaryRepository.findById(preliminary.id!!)
                    log.info("preliminary found: {}", prelim.get().preliminaryType?.name)
                    val prelimType = prelim.get().preliminaryType?.name


                    if(prelimType.toString().equals( "BOQ_PREPARATION")){
                        log.info("preliminary approved: {}", prelim)
//                        do something
                        val client = clientService.getClientById(preliminary.client.id)
                        client.clientStage = ClientStage.CONTRACT_SIGNING
                        client.nextStage = ClientStage.CONSTRUCTION
                        clientRepository.save(client)
                        PreliminaryStatus.COMPLETE

                    }else {
                        PreliminaryStatus.COMPLETE
                    }
                }
            }
            preliminary.rejectionRemarks = null
            CoroutineScope(Dispatchers.IO).launch {
                emailService.sendEmail(
                    "samuikumbu@gmail.com",
                    "YOUR ${preliminary.preliminaryType?.name} DOCUMENT IS READY",
                    "Dear ${preliminary.client?.firstName}, \nFind the document attached below for your review. \n Regards ",
                    userRepository.findByEmail(preliminary.client?.email!!)
//                    attachment = attachment
                )
            }
        }
        val savedPreliminary = preliminaryRepository.save(preliminary)

        return savedPreliminary

    }
    @Transactional
    fun updatePreliminaryStatus(preliminaryId: Long, status: PreliminaryStatus) {
        val preliminary = preliminaryRepository.findById(preliminaryId).orElseThrow()
        log.info("preliminary type ", preliminary.preliminaryType?.name)
        if(preliminary.preliminaryType?.name.toString()==="BOQ_PREPARATION"){
//                        do something
            val client = clientService.getClientById(preliminary.client!!.id)
            client.clientStage = ClientStage.CONTRACT_SIGNING
            client.nextStage = ClientStage.CONSTRUCTION
            clientRepository.save(client)
        }
        preliminary.status = status
        preliminary.rejectionRemarks = null
        preliminaryRepository.save(preliminary)
    }

    @Transactional
    fun submitBOQAmount(preliminaryId: Long, amount: BigDecimal): Preliminary {
        val preliminary = preliminaryRepository.findById(preliminaryId)
            .orElseThrow { IllegalArgumentException("Preliminary not found") }

        if (preliminary.preliminaryType?.name != "BOQ_PREPARATION") {
            throw IllegalArgumentException("This preliminary is not a BOQ preparation")
        }
        val client = clientService.getClientById(preliminary.client!!.id)
        client.boqAmount = amount
        clientRepository.save(client)

        preliminary.boqAmount = amount
        preliminary.status = PreliminaryStatus.PENDING_T_D_APPROVAL

        return preliminaryRepository.save(preliminary)
    }
}