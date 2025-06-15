package com.bbs.bbsapi.services

import com.bbs.bbsapi.dtos.ContractDTO
import com.bbs.bbsapi.models.ClientContract
import com.bbs.bbsapi.repositories.ClientContractRepository
import com.bbs.bbsapi.repositories.InvoiceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClientContractService(
    private val contractRepository: ClientContractRepository,
    private val invoiceService: InvoiceService,
    private val invoiceRepository: InvoiceRepository,
    private val milestoneChecklistService: MilestoneChecklistService
) {
    @Transactional
    fun createContract(contract: ClientContract, contractDTO: ContractDTO): Map<String, Any?> {
        val savedContract = contractRepository.save(contract)
        val invoiceResult = invoiceService.createContractInvoices(contractDTO)
        // Optionally update contract with mainInvoice reference
        val mainInvoice = invoiceResult["mainInvoice"]
        if (mainInvoice != null && mainInvoice is com.bbs.bbsapi.models.Invoice) {
            val updated = savedContract.copy(mainInvoice = mainInvoice)
            contractRepository.save(updated)
        }
        milestoneChecklistService.updateMilestoneStatus(contractDTO.clientId, "Contract signing", true)

        return mapOf(
            "contract" to savedContract,
            "mainInvoice" to invoiceResult["mainInvoice"],
            "installmentInvoices" to invoiceResult["installmentInvoices"]
        )
    }

    @Transactional(readOnly = true)
    fun getContractByClientId(clientId: Long): ClientContract? {
        return contractRepository.findByClientId(clientId)
    }

    @Transactional
    fun updateContract(contractId: Long, updated: ClientContract): ClientContract? {
        val existing = contractRepository.findById(contractId).orElse(null) ?: return null
        val merged = existing.copy(
            projectName = updated.projectName,
            boqAmount = updated.boqAmount,
            projectStartDate = updated.projectStartDate,
            projectDuration = updated.projectDuration,
            contractFileUrl = updated.contractFileUrl,
            status = updated.status,
            updatedAt = java.time.LocalDateTime.now()
        )
        return contractRepository.save(merged)
    }

    @Transactional(readOnly = true)
    fun getContractWithInvoicesByClientId(clientId: Long): Map<String, Any?>? {
        val contract = contractRepository.findByClientId(clientId) ?: return null
        // Fetch main invoice (MAIN_PROFORMA) for this client
        val mainInvoice = invoiceRepository.findByClientIdAndInvoiceType(clientId, com.bbs.bbsapi.enums.InvoiceType.MAIN_PROFORMA)
        // Fetch all INSTALLMENT invoices for this client
        val installmentInvoices = invoiceRepository.findByClientId(clientId)
            .filter { it.invoiceType == com.bbs.bbsapi.enums.InvoiceType.INSTALLMENT }
        return mapOf(
            "contract" to contract,
            "mainInvoice" to mainInvoice,
            "installmentInvoices" to installmentInvoices
        )
    }
} 