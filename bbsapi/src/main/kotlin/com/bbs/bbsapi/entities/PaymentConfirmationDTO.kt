package com.bbs.bbsapi.entities

data class PaymentConfirmationDTO(
    val paymentMethod: String,
    val amountPaid: Double,
    val reference: String
) 