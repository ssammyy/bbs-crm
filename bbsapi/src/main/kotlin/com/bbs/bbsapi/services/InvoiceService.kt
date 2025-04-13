package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.InvoiceResponse
import com.bbs.bbsapi.entities.PdfInvoiceDTO
import com.bbs.bbsapi.entities.PdfInvoiceItemDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.Invoice
import com.bbs.bbsapi.models.InvoiceItem
import com.bbs.bbsapi.repos.InvoiceRepository

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val pdfGenerator: PdfGenerator,
    private val clientService: ClientService
) {

    @Transactional
    fun createInvoice(request: PdfInvoiceDTO): InvoiceResponse {
        // Fetch the client
        val client = clientService.getClientById(request.clientId)

        // Create invoice entity from DTO
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
        )

        // Map items from DTO to entity
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

        // Validate total matches sum of item totals
        val calculatedTotal = invoiceItems.sumOf { it.totalPrice }
        require(calculatedTotal == invoice.total) { "Total (${invoice.total}) does not match sum of item totals ($calculatedTotal)" }

        val savedInvoice = invoiceRepository.save(invoice)

        // Update client stage based on invoice type
        when (request.invoiceType) {
            InvoiceType.PROFORMA -> {
                clientService.changeClientStatus(
                    ClientStage.PROFORMA_INVOICE_PENDING_DIRECTOR_APPROVAL,
                    client,
                    ClientStage.PENDING_SITE_VISIT,
                    "Proforma invoice generated"
                )
            }
            InvoiceType.SITE_VISIT -> {
                clientService.changeClientStatus(
                    ClientStage.PENDING_CLIENT_SITE_VISIT_PAYMENT,
                    client,
                    ClientStage.PENDING_SITE_VISIT,
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
                // Handle open invoices if needed
            }
        }

        // Generate PDF using the original DTO
        val pdfBytes = pdfGenerator.generateInvoicePdf(convertToPdfDto(savedInvoice))

        return InvoiceResponse(
            invoiceId = savedInvoice.id,
            invoiceNumber = savedInvoice.invoiceNumber,
            pdfContent = pdfBytes
        )
    }

    @Transactional(readOnly = true)
    fun getInvoicePdf(clientId: Long): ByteArray {
        val invoice = invoiceRepository.findByClientId(clientId)
            ?: throw IllegalArgumentException("No invoice found for client ID $clientId")
        return pdfGenerator.generateInvoicePdf(convertToPdfDto(invoice))
    }

    @Transactional
    fun acceptInvoice(clientId: Long, stage: ClientStage, invoiceType: InvoiceType): Client {
        val invoice = invoiceRepository.findByClientIdAndInvoiceType(clientId, invoiceType)
            ?: throw IllegalArgumentException("No invoice found for client ID $clientId")
        println("here >>")

        val client = clientService.getClientById(clientId)?: throw IllegalArgumentException("Client not found for client ID $clientId")
        // Update client stage based on the current stage
        when (stage) {
            ClientStage.PROFORMA_INVOICE_PENDING_DIRECTOR_APPROVAL -> {
                println("saving invoice ...")
                invoice.directorApproved = true
                invoiceRepository.save(invoice)
                clientService.changeClientStatus(
                    ClientStage.GENERATE_SITE_VISIT_INVOICE,
                    client,
                    ClientStage.PENDING_CLIENT_SITE_VISIT_PAYMENT,
                    "Director approved proforma invoice"
                )
            }
            ClientStage.GENERATE_SITE_VISIT_INVOICE -> {
                clientService.changeClientStatus(
                    ClientStage.GENERATE_SITE_VISIT_INVOICE,
                    client,
                    ClientStage.PENDING_CLIENT_SITE_VISIT_PAYMENT,
                    "Director approved site visit invoice"
                )
            }
            ClientStage.GENERATE_DRAWINGS_INVOICE -> {
                clientService.changeClientStatus(
                    ClientStage.GENERATE_DRAWINGS_INVOICE,
                    client,
                    ClientStage.PENDING_CLIENT_DRAWINGS_PAYMENT,
                    "Director approved drawings invoice"
                )
            }
            ClientStage.GENERATE_BOQ_PREPARATION_INVOICE -> {
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
        val invoice = invoiceRepository.findByClientId(clientId)
            ?: throw IllegalArgumentException("No invoice found for client ID $clientId")
        invoice.directorApproved = false
        invoice.rejectionRemarks = remarks
        invoiceRepository.save(invoice)
        val client = clientService.getClientById(clientId)

        // Revert to the previous stage based on the current stage
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

    private fun convertToPdfDto(invoice: Invoice): PdfInvoiceDTO {
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
        )
    }
}

