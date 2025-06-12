package com.bbs.bbsapi.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pending_agent_approvals")
data class PendingAgentApproval(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var username: String? = null,

    @Column(nullable = false)
    var email: String? = null,

    @Column(nullable = false)
    var phonenumber: String? = null,

    @Column
    var paymentMethod: String? = null,

    @Column
    var bankName: String? = null,

    @Column
    var bankAccountNumber: String? = null,

    @Column
    var bankBranch: String? = null,

    @Column
    var bankAccountHolderName: String? = null,

    @Column
    var nextOfKinIdNumber: String? = null,

    @Column
    var nextOfKinName: String? = null,

    @Column
    var nextOfKinPhoneNumber: String? = null,

    @Column
    var commissionPercentage: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    var role: Role? = null,

    @Column(nullable = false)
    var status: ApprovalStatus = ApprovalStatus.PENDING,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var approvedAt: LocalDateTime? = null,

    @Column
    var approvedBy: String? = null,

    @Column
    var rejectionReason: String? = null
) : BaseEntity()

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
} 