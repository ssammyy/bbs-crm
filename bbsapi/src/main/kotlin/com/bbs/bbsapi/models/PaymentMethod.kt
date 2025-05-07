package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.PaymentMethod
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime



@Entity
@Table(name = "receipts")
data class Receipt(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    val invoice: Invoice? = null,

    @Column(nullable = false)
    val paymentDate: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod? = null,

    @Column(nullable = false)
    val amountPaid: Double? = null,

    @Column(nullable = true)
    val transactionId: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)