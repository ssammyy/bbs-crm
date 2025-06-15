package com.bbs.bbsapi.models
import com.bbs.bbsapi.enums.PreliminaryStatus
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "preliminary")
data class Preliminary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    val client: Client? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preliminary_type_id", nullable = false)
    var preliminaryType: PreliminaryType? = null,

    @Column(name = "client_invoiced_for_approval", nullable = false)
    var clientInvoicedForApproval: Boolean = false,

    @Column(name = "client_paid_for_approval", nullable = false)
    var clientPaidForApproval: Boolean = false,

    @Column(name = "boq_amount", nullable = true)
    var boqAmount: BigDecimal? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = true)
    @JsonIgnore
    var invoice: Invoice? = null,

    @Column(name = "invoiced", nullable = false)
    var invoiced: Boolean = false,

    @Column(name = "invoice_cleared", nullable = false)
    var invoiceClearedFlag: Boolean = false,

   @Column(name = "county_receipt_uploaded", nullable = false)
    var countyReceiptUploaded: Boolean = false,

    @Column(name = "county_approved-document_uploaded", nullable = false)
    var countyApprovedDocumentUploaded: Boolean = false,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: PreliminaryStatus? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var rejectionRemarks: String? = null,
)