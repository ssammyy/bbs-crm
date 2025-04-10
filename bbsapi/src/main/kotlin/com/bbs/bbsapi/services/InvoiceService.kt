package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.PdfInvoiceDTO
import com.bbs.bbsapi.entities.InvoiceResponse
import com.bbs.bbsapi.entities.PdfInvoiceItemDTO
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.Invoice
import com.bbs.bbsapi.models.InvoiceItem
import com.bbs.bbsapi.repos.InvoiceRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val pdfGenerator: PdfGenerator,
    private val clientService: ClientService
) {

    @Transactional
    fun createInvoice(request: PdfInvoiceDTO): InvoiceResponse {
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
                invoice = invoice, // Explicitly set the invoice reference
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
        val client = clientService.getClientById(savedInvoice.clientId);

//        change client status
        clientService.changeClientStatus(ClientStage.INVOICE_PENDING_DIRECTOR_APPROVAL, client, ClientStage.PENDING_SITE_VISIT, "proforma Invoice generated")


        // Generate PDF using the original DTO
        val pdfBytes = pdfGenerator.generateInvoicePdf(convertToPdfDto(savedInvoice))

        return InvoiceResponse(
            invoiceId = savedInvoice.id,
            invoiceNumber = savedInvoice.invoiceNumber,
            pdfContent = pdfBytes
        )
    }

    @Transactional(readOnly = true)
    fun getInvoicePdf(invoiceId: Long): ByteArray {
        val invoice = invoiceRepository.findByClientId(invoiceId)
        return pdfGenerator.generateInvoicePdf(convertToPdfDto(invoice))
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

    fun acceptInvoice(clientId: Long): ResponseEntity<Client> {
        val invoice = invoiceRepository.findByClientId(clientId)
        invoice.directorApproved = true
        invoiceRepository.save(invoice)
        val client = clientService.getClientById(clientId)
        clientService.changeClientStatus(ClientStage.GENERATE_SITE_VISIT_INVOICE, client, ClientStage.PENDING_CLIENT_SITE_VISIT_PAYMENT, "Director approved invoice")

        return ResponseEntity.ok(client)

    }
}






