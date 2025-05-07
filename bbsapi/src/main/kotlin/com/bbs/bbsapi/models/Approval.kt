package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.ApprovalStage
import com.bbs.bbsapi.enums.ApprovalStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "approvals")
data class Approval(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preliminary_id", nullable = true)
    val preliminary: Preliminary? = null,

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "invoice_id", nullable = true)
//    val invoice: Invoice? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false)
    val approvedBy: User? = null,

    @Column(name = "technical_approved", nullable = false)
    val technicalApproved: Boolean = false,

    @Column(name = "director_approved", nullable = false)
    val directorApproved: Boolean = false,

    @Column(name = "status", nullable = false)
    val status: ApprovalStatus, // e.g., "APPROVED", "REJECTED", "PENDING"

    @Column(name = "remarks")
    val remarks: String? = null,

    @Column(name = "approval_stage")
    val approvalStage: ApprovalStage? = null,


    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()


)