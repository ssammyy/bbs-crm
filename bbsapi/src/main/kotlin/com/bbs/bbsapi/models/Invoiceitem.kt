package com.bbs.bbsapi.models

import jakarta.persistence.*
@Entity
@Table(name = "invoice_items")
data class InvoiceItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    var invoice: Invoice? = null, // Nullable for instantiation, set by service

    @Column(nullable = false)
    val description: String = "",

    @Column(nullable = false)
    val quantity: Int = 0,

    @Column(nullable = false)
    val unitPrice: Double = 0.0,

    @Column(nullable = false)
    val totalPrice: Double = 0.0
) {
    // No explicit constructor or init block - rely on defaults and service validation
}