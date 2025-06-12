package com.bbs.bbsapi.repositories

import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Invoice
import com.bbs.bbsapi.models.InvoiceItem
import com.bbs.bbsapi.models.ProformaInvoice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface InvoiceRepository : JpaRepository<Invoice, Long> {
    fun findByClientId(clientId: Long): List<Invoice>
    fun findByClientIdAndInvoiceType(clientId: Long, invoiceType: InvoiceType): Invoice?
    fun findByClientIdAndPreliminaryId(clientId: Long, preliminaryId: Long): Invoice?
    fun findByClientIdAndGovernmentApprovalType(clientId: Long, governmentApprovalType: String): Invoice?
    fun findByClearedTrue(): List<Invoice>
    fun findByClearedFalse(): List<Invoice>
    fun findByClearedFalseAndInvoiceTypeNot(invoiceType: InvoiceType): List<Invoice>
    fun countByClearedTrue(): Long
    fun countByClearedFalse(): Long
    fun findByPreliminaryId(preliminaryId: Long): Invoice?
    fun findByParentInvoiceId(parentInvoiceId: Long): List<Invoice>
    fun findByClientIdAndClearedTrue(clientId: Long): List<Invoice>
    fun findByClientIdAndClearedFalseAndInvoiceTypeNot(clientId: Long, invoiceType: InvoiceType): List<Invoice>
    fun countByClientIdAndClearedTrue(clientId: Long): Long
    fun countByClientIdAndClearedFalse(clientId: Long): Long
    fun findByClientIdIn(clientIdList: List<Long>): List<Invoice>
    fun findByClearedTrueAndDateIssuedBetween(start: LocalDate?, end: LocalDate?): List<Invoice>
    fun findByClientIdInAndClearedTrueAndDateIssuedBetween(clientIdList: List<Long>, start: LocalDate?, end: LocalDate?): List<Invoice>
}

@Repository
interface InvoiceItemRepository : JpaRepository<InvoiceItem, Long> {
}

@Repository
interface ProformaInvoiceRepository : JpaRepository<ProformaInvoice, Long> {
    fun findByClientId(clientId: Long): ProformaInvoice?
}