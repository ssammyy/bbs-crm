package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.InvoiceResponse
import com.bbs.bbsapi.entities.PdfInvoiceDTO
import com.bbs.bbsapi.entities.PdfInvoiceItemDTO
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.Invoice
import com.bbs.bbsapi.models.InvoiceItem
import com.bbs.bbsapi.models.ProformaInvoice
import com.bbs.bbsapi.repos.InvoiceRepository
import com.bbs.bbsapi.repos.PreliminaryRepository
import com.bbs.bbsapi.repos.ProformaInvoiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val preliminaryRepository: PreliminaryRepository,
    private val proformaInvoiceRepository: ProformaInvoiceRepository,
    private val pdfGenerator: PdfGenerator,
    private val clientService: ClientService,
    private val emailService: EmailService
) {

    @Transactional
    fun createInvoice(request: PdfInvoiceDTO): InvoiceResponse {
        val client = clientService.getClientById(request.clientId)
        val preliminary = request.preliminaryId?.let {
            preliminaryRepository.findById(it)
                .orElseThrow { IllegalArgumentException("Preliminary not found: $it") }
        }

        val invoice = Invoice(
            invoiceNumber = request.invoiceNumber,
            dateIssued = LocalDate.now(),
            clientId = request.clientId,
            clientName = request.clientName,
            clientPhone = request.clientPhone,
            projectName = request.projectName,
            items = mutableListOf(),
            total = request.total,
            invoiceType = request.invoiceType,
            preliminary = preliminary
        )

        if (preliminary != null) {
            preliminary?.invoiced = true
            preliminaryRepository.save(preliminary!!)
        }


        val invoiceItems = request.items.map { itemDto ->
            InvoiceItem(
                invoice = invoice,
                description = itemDto.description,
                quantity = itemDto.quantity,
                unitPrice = itemDto.unitPrice.toDouble(),
                totalPrice = itemDto.totalPrice.toDouble()
            )
        }.toMutableList()

        invoice.items.addAll(invoiceItems)

        val calculatedTotal = invoiceItems.sumOf { it.totalPrice }
        require(calculatedTotal == invoice.total) { "Total (${invoice.total}) does not match sum of item totals ($calculatedTotal)" }

        val savedInvoice = invoiceRepository.save(invoice)

        // Update proforma invoice
        updateProformaInvoice(client.id, savedInvoice)

        // Update client stage and preliminary status
        when (request.invoiceType) {
            InvoiceType.PROFORMA -> {
                CoroutineScope(Dispatchers.IO).launch {
                    emailService.sendEmail(
                        "samuikumbu@gmail.com",
                        "CLIENT PROFORMA INVOICE PENDING YOUR ACTION",
                        "Kindly log in to the BBS_CRM system and approve ${client.firstName} ${client.lastName} proforma invoice"
                    )
                }
                clientService.changeClientStatus(
                    ClientStage.PROFORMA_INVOICE_PENDING_DIRECTOR_APPROVAL,
                    client,
                    ClientStage.PENDING_SITE_VISIT,
                    "Proforma invoice generated"
                )
            }
            InvoiceType.SITE_VISIT -> {

                clientService.changeClientStatus(
                    ClientStage.PENDING_DIRECTOR_SITE_VISIT_INVOICE_APPROVAL,
                    client,
                    ClientStage.PENDING_CLIENT_SITE_VISIT_PAYMENT,
                    "Site visit invoice generated"
                )
            }
            InvoiceType.ARCHITECTURAL_DRAWINGS -> {

                clientService.changeClientStatus(
                    ClientStage.PENDING_CLIENT_DRAWINGS_PAYMENT,
                    client,
                    ClientStage.ARCHITECTURAL_DRAWINGS_SKETCH,
                    "Architectural drawings invoice generated"
                )
            }
            InvoiceType.BOQ -> {

                clientService.changeClientStatus(
                    ClientStage.GENERATE_BOQ_PREPARATION_INVOICE,
                    client,
                    ClientStage.PENDING_CLIENT_BOQ_PREPARATION_PAYMENT,
                    "BOQ preparation invoice generated"
                )
            }
            InvoiceType.OPEN -> {

            }
            InvoiceType.PRELIMINARY->{

            }
        }

        val pdfBytes = pdfGenerator.generateInvoicePdf(convertToPdfDto(savedInvoice))

        return InvoiceResponse(
            invoiceId = savedInvoice.id,
            invoiceNumber = savedInvoice.invoiceNumber,
            pdfContent = pdfBytes
        )
    }

    @Transactional(readOnly = true)
    fun getInvoicePdf(clientId: Long, invoiceType: InvoiceType): ByteArray {
        if (invoiceType == InvoiceType.PROFORMA) {
        return pdfGenerator.generateInvoicePdf( getProformaInvoice(clientId)!!)

        }
        val invoice = invoiceRepository.findByClientIdAndInvoiceType(clientId, invoiceType)

        return pdfGenerator.generateInvoicePdf(convertToPdfDto(invoice))
    }

    @Transactional(readOnly = true)
    fun getPreliminaryInvoicePdf(clientId: Long, preliminaryId: Long): ByteArray {
        val invoice = invoiceRepository.findByClientIdAndPreliminaryId(clientId, preliminaryId)

        return pdfGenerator.generateInvoicePdf(convertToPdfDto(invoice))
    }

    @Transactional(readOnly = true)
    fun getProformaInvoice(clientId: Long): PdfInvoiceDTO? {
        val proforma = proformaInvoiceRepository.findByClientId(clientId)
            ?: return null
        val client = clientService.getClientById(clientId)
        return PdfInvoiceDTO(
            invoiceNumber = "PROFORMA-${clientId}",
            dateIssued = LocalDate.now(),
            clientId = proforma.clientId!!,
            clientName = client.firstName,
            clientPhone = client.phoneNumber,
            projectName = client.projectName,
            items = proforma.invoices.flatMap { it.items }.map { item ->
                PdfInvoiceItemDTO(
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice
                )
            }.toMutableList(),
            total = proforma.totalAmount.toDouble() ,
            invoiceType = InvoiceType.PROFORMA
        )
    }
    @Transactional
    fun acceptInvoice(clientId: Long, stage: ClientStage, invoiceType: InvoiceType): Client {
        val invoice = invoiceRepository.findByClientIdAndInvoiceType(clientId, invoiceType)
            ?: throw IllegalArgumentException("No invoice found for client ID $clientId")
        val client = clientService.getClientById(clientId)
            ?: throw IllegalArgumentException("Client not found for client ID $clientId")

        when (stage) {
            ClientStage.PROFORMA_INVOICE_PENDING_DIRECTOR_APPROVAL -> {
                invoice.directorApproved = true
                invoiceRepository.save(invoice)
                clientService.changeClientStatus(
                    ClientStage.GENERATE_SITE_VISIT_INVOICE,
                    client,
                    ClientStage.PENDING_CLIENT_SITE_VISIT_PAYMENT,
                    "Director approved proforma invoice"
                )
            }

            ClientStage.PENDING_DIRECTOR_SITE_VISIT_INVOICE_APPROVAL -> {
                invoice.directorApproved = true
                invoiceRepository.save(invoice)
                clientService.changeClientStatus(
                    ClientStage.PENDING_CLIENT_SITE_VISIT_PAYMENT,
                    client,
                    ClientStage.PENDING_SITE_VISIT,
                    "Director approved site visit invoice"
                )
            }
            ClientStage.GENERATE_DRAWINGS_INVOICE -> {
                invoice.directorApproved = true
                invoiceRepository.save(invoice)

                clientService.changeClientStatus(
                    ClientStage.GENERATE_DRAWINGS_INVOICE,
                    client,
                    ClientStage.PENDING_CLIENT_DRAWINGS_PAYMENT,
                    "Director approved drawings invoice"
                )
            }
            ClientStage.GENERATE_BOQ_PREPARATION_INVOICE -> {
                invoice.directorApproved = true
                invoiceRepository.save(invoice)

                clientService.changeClientStatus(
                    ClientStage.GENERATE_BOQ_PREPARATION_INVOICE,
                    client,
                    ClientStage.PENDING_CLIENT_BOQ_PREPARATION_PAYMENT,
                    "Director approved BOQ preparation invoice"
                )
            }
            else -> throw IllegalStateException("Invalid stage for invoice approval: $stage")
        }

        return client
    }

    @Transactional
    fun rejectInvoice(clientId: Long, remarks: String, stage: ClientStage): Client {
        val invoice = invoiceRepository.findByClientIdAndInvoiceType(clientId, InvoiceType.OPEN)
            ?: throw IllegalArgumentException("No invoice found for client ID $clientId")
        invoice.directorApproved = false
        invoice.rejectionRemarks = remarks
        invoiceRepository.save(invoice)
        val client = clientService.getClientById(clientId)
            ?: throw IllegalArgumentException("Client not found for client ID $clientId")

        when (stage) {
            ClientStage.PROFORMA_INVOICE_PENDING_DIRECTOR_APPROVAL -> {
                clientService.changeClientStatus(
                    ClientStage.PROFORMA_INVOICE_PENDING_DIRECTOR_APPROVAL,
                    client,
                    ClientStage.PROFORMA_INVOICE_GENERATION,
                    "Director rejected proforma invoice: $remarks"
                )
            }
            ClientStage.GENERATE_SITE_VISIT_INVOICE -> {

                clientService.changeClientStatus(
                    ClientStage.GENERATE_SITE_VISIT_INVOICE,
                    client,
                    ClientStage.PENDING_SITE_VISIT,
                    "Director rejected site visit invoice: $remarks"
                )
            }
            ClientStage.GENERATE_DRAWINGS_INVOICE -> {

                clientService.changeClientStatus(
                    ClientStage.GENERATE_DRAWINGS_INVOICE,
                    client,
                    ClientStage.PENDING_SITE_VISIT,
                    "Director rejected drawings invoice: $remarks"
                )
            }
            ClientStage.GENERATE_BOQ_PREPARATION_INVOICE -> {

                clientService.changeClientStatus(
                    ClientStage.GENERATE_BOQ_PREPARATION_INVOICE,
                    client,
                    ClientStage.PENDING_CLIENT_BOQ_APPROVALS,
                    "Director rejected BOQ preparation invoice: $remarks"
                )
            }
            else -> throw IllegalStateException("Invalid stage for invoice rejection: $stage")
        }

        return client
    }
    private fun generateRandomString(length: Int = 8): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    private fun updateProformaInvoice(clientId: Long, invoice: Invoice) {
        val proforma = proformaInvoiceRepository.findByClientId(clientId)
            ?: ProformaInvoice(clientId = clientId, totalAmount = BigDecimal.ZERO, createdAt = LocalDateTime.now())
        proforma.totalAmount = proforma.totalAmount.add(invoice.total.toBigDecimal())
        proforma.updatedAt = LocalDateTime.now()
        proforma.invoices.add(invoice)
        proforma.invoiceNumber = generateRandomString(8)
        proformaInvoiceRepository.save(proforma)
    }

    fun convertToPdfDto(invoice: Invoice): PdfInvoiceDTO {
        return PdfInvoiceDTO(
            invoiceNumber = invoice.invoiceNumber,
            dateIssued = invoice.dateIssued,
            clientId = invoice.clientId,
            clientName = invoice.clientName,
            clientPhone = invoice.clientPhone,
            projectName = invoice.projectName,
            items = invoice.items.map { item ->
                PdfInvoiceItemDTO(
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice
                )
            }.toMutableList(),
            total = invoice.total,
            invoiceType = invoice.invoiceType,
            preliminaryId = invoice.preliminary?.id
        )
    }

    fun getInvoiceByPreliminaryId(preliminaryId: Long): Invoice? {
        return invoiceRepository.findByPreliminaryId(preliminaryId)
    }
    fun getInvoicesByClientId(clientId: Long): List<Invoice> {
        return invoiceRepository.findByClientId(clientId)
    }
}