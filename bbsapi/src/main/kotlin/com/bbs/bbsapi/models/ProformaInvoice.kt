package com.bbs.bbsapi.models

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "proforma_invoice")
data class ProformaInvoice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "client_id", nullable = false)
    val clientId: Long? = null,

    @Column(name = "invoice_number", nullable = false)
    var invoiceNumber: String? = null,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @ManyToMany
    @JoinTable(
        name = "proforma_invoice_invoices",
        joinColumns = [JoinColumn(name = "proforma_invoice_id")],
        inverseJoinColumns = [JoinColumn(name = "invoice_id")]
    )
    val invoices: MutableList<Invoice> = mutableListOf()
)