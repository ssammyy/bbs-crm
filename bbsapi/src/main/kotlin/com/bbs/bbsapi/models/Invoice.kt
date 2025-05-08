package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.InvoiceType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "invoices")
data class Invoice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val invoiceNumber: String = "",

    @Column(nullable = false)
    val dateIssued: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    val clientId: Long = 0L,

    @Column(nullable = false)
    val clientName: String = "",

    @Column(nullable = false)
    val clientPhone: String = "",

    @Column(nullable = false)
    val projectName: String = "",

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val items: MutableList<InvoiceItem> = mutableListOf(),

    @Column(nullable = false)
    val total: Double = 0.0,

    @Column(nullable = false)
    val clientApproved: Boolean = false,

    @Column(nullable = false)
    var directorApproved: Boolean = false,

    @Column(nullable = false)
    var invoiceReconciled: Boolean = false,

    @Enumerated(EnumType.STRING)
    val invoiceType: InvoiceType = InvoiceType.PROFORMA,

    @Column(nullable = true)
    var rejectionRemarks: String = "",

    @Column(nullable = false)
    var cleared: Boolean = false,

    @Column(nullable = false)
    var pendingBalance : Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preliminary_id", nullable = true)
    var preliminary: Preliminary? = null,

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val receipts: MutableList<Receipt> = mutableListOf(),

    @Column(nullable = false)
    var balance: Double = total,

    @Column(nullable = false)
    var discountPercentage: Double = 0.0,

    @Column(nullable = false)
    var discountAmount: Double = 0.0,

    @Column(nullable = false)
    var subtotal: Double = total,

    @Column(nullable = false)
    var finalTotal: Double = total
)