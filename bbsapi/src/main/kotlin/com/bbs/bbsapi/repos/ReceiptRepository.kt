package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.Receipt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ReceiptRepository : JpaRepository<Receipt, Long> {
    fun findByInvoiceId(invoiceId: Long): List<Receipt>
    fun findReceiptsByInvoiceClientId(invoiceClientId: Long): List<Receipt>
    @Query("SELECT r FROM Receipt r WHERE r.invoice.clientId = :clientId")
    fun findByClientId(clientId: Long): List<Receipt>

}