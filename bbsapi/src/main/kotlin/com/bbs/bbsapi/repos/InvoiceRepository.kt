package com.bbs.bbsapi.repos

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Invoice
import com.bbs.bbsapi.models.InvoiceItem
import com.bbs.bbsapi.models.ProformaInvoice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository: JpaRepository<Invoice, Long> {

    fun findByClientId(clientId: Long): List<Invoice>
    fun findByClientIdAndInvoiceType(clientId: Long, invoiceType: InvoiceType): Invoice
    fun findByClientIdAndPreliminaryId(clientId: Long, preliminaryId: Long): Invoice
    fun findByClearedTrue(): List<Invoice>
    fun findByClearedFalse(): List<Invoice>
    fun findByClearedFalseAndInvoiceTypeNot(invoiceType: InvoiceType): List<Invoice>
    fun countByClearedTrue(): Long
    fun countByClearedFalse(): Long
    fun findByPreliminaryId(preliminaryId: Long): Invoice
//    fun findByOriginalInvoiceId(originalInvoiceId: Long): List<Invoice>
}
@Repository
interface InvoiceItemRepository: JpaRepository<InvoiceItem, Long> {
}

@Repository
interface ProformaInvoiceRepository : JpaRepository<ProformaInvoice, Long> {
    fun findByClientId(clientId: Long): ProformaInvoice?
}