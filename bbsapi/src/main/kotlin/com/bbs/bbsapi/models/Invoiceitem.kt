package com.bbs.bbsapi.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "invoice_items")
data class InvoiceItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    var invoice: Invoice? = null,

    @Column(nullable = false)
    val description: String = "",

    @Column(nullable = false)
    val quantity: Int = 0,

    @Column(nullable = false)
    val unitPrice: Double = 0.0,

    @Column(nullable = false)
    val totalPrice: Double = 0.0
)