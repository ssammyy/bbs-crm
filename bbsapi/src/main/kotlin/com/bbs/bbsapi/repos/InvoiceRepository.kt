package com.bbs.bbsapi.repos

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Invoice
import com.bbs.bbsapi.models.InvoiceItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository: JpaRepository<Invoice, Long> {

    fun findByClientId(clientId: Long): Invoice
    fun findByClientIdAndInvoiceType(clientId: Long, invoiceType: InvoiceType): Invoice
}
@Repository
interface InvoiceItemRepository: JpaRepository<InvoiceItem, Long> {
}