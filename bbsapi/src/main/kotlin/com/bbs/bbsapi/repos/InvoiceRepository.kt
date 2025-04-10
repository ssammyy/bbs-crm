package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.Invoice
import com.bbs.bbsapi.models.InvoiceItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository: JpaRepository<Invoice, Long> {

    fun findByClientId(clientId: Long): Invoice
}
@Repository
interface InvoiceItemRepository: JpaRepository<InvoiceItem, Long> {
}