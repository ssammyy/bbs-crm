package com.bbs.bbsapi.entities

import com.bbs.bbsapi.enums.InvoiceType
import java.time.LocalDate

data class PdfInvoiceDTO(
    val invoiceNumber: String,
    val dateIssued: LocalDate,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val projectName: String,
    val items: MutableList<PdfInvoiceItemDTO>,
    val total: Double,
    val invoiceType: InvoiceType,
    val preliminaryId: Long? = null,
    val discountPercentage: Double = 0.0,
    val discountAmount: Double = 0.0,
    val subtotal: Double = total,
    val finalTotal: Double = total,
    val parentInvoiceId: Long? = null,
    val countyInvoiceType: String? = null,
)

data class PdfInvoiceItemDTO(
    val description: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)

data class InvoiceResponse(
    val invoiceId: Long,
    val invoiceNumber: String,
    val pdfContent: ByteArray
)

data class AcceptInvoiceRequest(
    val stage: String,
    val invoiceType: InvoiceType?=null,
)

data class RejectInvoiceRequest(
    val stage: String,
    val remarks: String
)




