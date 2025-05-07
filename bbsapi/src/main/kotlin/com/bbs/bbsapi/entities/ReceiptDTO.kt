package com.bbs.bbsapi.entities

import com.bbs.bbsapi.enums.PaymentMethod
import java.time.LocalDate
import java.time.LocalDateTime

data class ReceiptDTO(
    val id: Long = 0,
    val invoiceId: Long,
    val paymentDate: LocalDate,
    val paymentMethod: PaymentMethod,
    val amountPaid: Double,
    val transactionId: String? = null,
    val createdAt: LocalDateTime? = null
)