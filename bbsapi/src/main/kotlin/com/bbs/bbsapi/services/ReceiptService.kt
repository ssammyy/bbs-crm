package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.ReceiptDTO
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Receipt
import com.bbs.bbsapi.repos.InvoiceRepository
import com.bbs.bbsapi.repos.ReceiptRepository
import com.bbs.bbsapi.repos.PreliminaryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class ReceiptService(
    private val receiptRepository: ReceiptRepository,
    private val invoiceRepository: InvoiceRepository,
    private val clientService: ClientService,
    private val preliminaryRepository: PreliminaryRepository
) {
    @Transactional
    fun createReceipt(receiptDTO: ReceiptDTO): ReceiptDTO {


        val savedReceipt = try {
            val invoice = invoiceRepository.findById(receiptDTO.invoiceId)
                .orElseThrow { IllegalArgumentException("Invoice with ID ${receiptDTO.invoiceId} not found") }
            val client = clientService.getClientById(invoice.clientId)

            if (receiptDTO.amountPaid <= 0) {
                throw IllegalArgumentException("Amount paid must be greater than zero")
            }
            if (receiptDTO.amountPaid > invoice.balance) {
                throw IllegalArgumentException("Amount paid (${receiptDTO.amountPaid}) exceeds remaining balance (${invoice.balance})")
            }

            val receipt = Receipt(
                invoice = invoice,
                paymentDate = receiptDTO.paymentDate,
                paymentMethod = receiptDTO.paymentMethod,
                amountPaid = receiptDTO.amountPaid,
                transactionId = receiptDTO.transactionId,
                createdAt = LocalDateTime.now()
            )

            invoice.balance -= receiptDTO.amountPaid
            invoice.invoiceReconciled = invoice.balance <= 0
            if (invoice.invoiceReconciled) {
                invoice.cleared = true
                invoice.pendingBalance = false
            } else {
                invoice.pendingBalance = true
                if (invoice.invoiceType === InvoiceType.SITE_VISIT && !client.siteVisitDone) {
                    clientService.changeClientStatus(
                        ClientStage.PENDING_SITE_VISIT,
                        client,
                        ClientStage.REQUIREMENTS_PENDING_DIRECTOR_APPROVAL,
                        "Client Paid for site visit"
                    )
                }
            }

            if (invoice.parentInvoice != null) {
                val parentInvoice = invoice.parentInvoice!!
                parentInvoice.balance -= receiptDTO.amountPaid
                parentInvoice.invoiceReconciled = parentInvoice.balance <= 0
                parentInvoice.pendingBalance = !parentInvoice.invoiceReconciled
                parentInvoice.cleared = parentInvoice.invoiceReconciled
                invoiceRepository.save(parentInvoice)
                println("Updated parent invoice ${parentInvoice.id}: balance=${parentInvoice.balance}, reconciled=${parentInvoice.invoiceReconciled}")
            }

            invoiceRepository.save(invoice)
            println("Updated invoice ${invoice.id}: balance=${invoice.balance}, reconciled=${invoice.invoiceReconciled}")

            if (invoice.invoiceReconciled) {
                val prelimsToClear = preliminaryRepository.findByInvoiceId(invoice.id)
                prelimsToClear.forEach { prelim ->
                    println("Updating preliminary ${prelim.id} to invoiceClearedFlag=true")
                    prelim.invoiceClearedFlag = true
                    preliminaryRepository.save(prelim)
                }

                if (invoice.invoiceType === InvoiceType.SITE_VISIT && !client.siteVisitDone) {
                    clientService.changeClientStatus(
                        ClientStage.PENDING_SITE_VISIT,
                        client,
                        ClientStage.REQUIREMENTS_PENDING_DIRECTOR_APPROVAL,
                        "Client Paid for site visit"
                    )
                }
            }

           val savedReceipt = receiptRepository.save(receipt)
            return ReceiptDTO(
                id = savedReceipt.id,
                invoiceId = savedReceipt.invoice!!.id,
                invoiceNumber = savedReceipt.invoice!!.invoiceNumber!!,
                clientName = savedReceipt.invoice!!.clientName,
                paymentDate = savedReceipt.paymentDate,
                paymentMethod = savedReceipt.paymentMethod!!,
                amountPaid = savedReceipt.amountPaid!!,
                transactionId = savedReceipt.transactionId,
                createdAt = savedReceipt.createdAt
            )
        } catch (e: Exception) {
            e.printStackTrace()
            TODO("Not yet implemented")
        }


    }

    fun getReceiptsByClient(clientId: Long, search: String? = null, paymentMethod: String? = null): List<ReceiptDTO> {
        val receipts = receiptRepository.findReceiptsByInvoiceClientId(clientId)
        println("Found ${receipts.size} receipts")
        return receipts.map { receipt ->
            val invoice = receipt.invoice!!
            ReceiptDTO(
                id = receipt.id,
                invoiceId = invoice.id,
                invoiceNumber = invoice.invoiceNumber!!,
                clientName = invoice.clientName,
                paymentDate = receipt.paymentDate,
                paymentMethod = receipt.paymentMethod!!,
                amountPaid = receipt.amountPaid!!,
                transactionId = receipt.transactionId,
                createdAt = receipt.createdAt
            )
        }.filter { receipt ->
            val searchLower = search?.lowercase(Locale.getDefault()) ?: ""
            val matchesSearch = search.isNullOrEmpty() ||
                    receipt.invoiceNumber!!.lowercase(Locale.getDefault()).contains(searchLower) ||
                    receipt.clientName!!.lowercase(Locale.getDefault()).contains(searchLower) ||
                    (receipt.transactionId?.lowercase(Locale.getDefault())?.contains(searchLower) ?: false)
            val matchesPaymentMethod = paymentMethod.isNullOrEmpty() || receipt.paymentMethod.equals(paymentMethod)
            matchesSearch && matchesPaymentMethod
        }
    }

    fun getReceiptsForInvoice(invoiceId: Long): List<ReceiptDTO>? {
        TODO("Not yet implemented")
    }
}