package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.ReceiptDTO
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Receipt
import com.bbs.bbsapi.repos.InvoiceRepository
import com.bbs.bbsapi.repos.ReceiptRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReceiptService(
    private val receiptRepository: ReceiptRepository,
    private val invoiceRepository: InvoiceRepository,
    private val clientService: ClientService,
) {
    @Transactional
    fun createReceipt(receiptDTO: ReceiptDTO): ReceiptDTO {
        val invoice = invoiceRepository.findById(receiptDTO.invoiceId)
            .orElseThrow { IllegalArgumentException("Invoice with ID ${receiptDTO.invoiceId} not found") }
        val client = clientService.getClientById(invoice.clientId)
        // Validate amountPaid
        if (receiptDTO.amountPaid <= 0) {
            throw IllegalArgumentException("Amount paid must be greater than zero")
        }
        if (receiptDTO.amountPaid > invoice.balance) {
            throw IllegalArgumentException("Amount paid (${receiptDTO.amountPaid}) exceeds remaining balance (${invoice.balance})")
        }

        // Create receipt
        val receipt = Receipt(
            invoice = invoice,
            paymentDate = receiptDTO.paymentDate,
            paymentMethod = receiptDTO.paymentMethod,
            amountPaid = receiptDTO.amountPaid,
            transactionId = receiptDTO.transactionId,
            createdAt = LocalDateTime.now()
        )

        // Update invoice balance and reconciled status
        invoice.balance -= receiptDTO.amountPaid
        invoice.invoiceReconciled = invoice.balance <= 0
        if (invoice.invoiceReconciled) {
            invoice.cleared = true
            invoice.pendingBalance = false
            if (invoice.invoiceType=== InvoiceType.SITE_VISIT){
                clientService.changeClientStatus(
                    ClientStage.PENDING_SITE_VISIT,
                    client,
                    ClientStage.REQUIREMENTS_PENDING_DIRECTOR_APPROVAL,
                    "Client Paid for site visit"
                )

            }
        }else{
            invoice.pendingBalance = true
            if (invoice.invoiceType=== InvoiceType.SITE_VISIT){
                clientService.changeClientStatus(
                    ClientStage.PENDING_SITE_VISIT,
                    client,
                    ClientStage.REQUIREMENTS_PENDING_DIRECTOR_APPROVAL,
                    "Client Paid for site visit"
                )

            }
        }
        invoiceRepository.save(invoice)

        // Save receipt
        val savedReceipt = receiptRepository.save(receipt)

        // Return DTO
        return ReceiptDTO(
            id = savedReceipt.id,
            invoiceId = savedReceipt.invoice!!.id,
            paymentDate = savedReceipt.paymentDate,
            paymentMethod = savedReceipt.paymentMethod!!,
            amountPaid = savedReceipt.amountPaid!!,
            transactionId = savedReceipt.transactionId,
            createdAt = savedReceipt.createdAt
        )
    }

    fun getReceiptsForInvoice(invoiceId: Long): List<ReceiptDTO> {
        val receipts = receiptRepository.findByInvoiceId(invoiceId)
        return receipts.map { receipt ->
            ReceiptDTO(
                id = receipt.id,
                invoiceId = receipt.invoice!!.id,
                paymentDate = receipt.paymentDate,
                paymentMethod = receipt.paymentMethod!!,
                amountPaid = receipt.amountPaid!!,
                transactionId = receipt.transactionId,
                createdAt = receipt.createdAt
            )
        }
    }
}