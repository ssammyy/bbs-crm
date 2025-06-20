package com.bbs.bbsapi.models

import jakarta.persistence.*
import jdk.jfr.Percentage

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    var username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = false)
    var phonenumber: String,

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

    @Column(nullable = false)
    var termsAccepted: Boolean = false,

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    var role: Role? = null,
) : BaseEntity() {
    constructor() : this(
        id = null,
        username = "",
        password = "",
        email = "",
        phonenumber = "",
        paymentMethod = null,
        bankName = null,
        bankAccountNumber = null,
        bankBranch = null,
        bankAccountHolderName = null,
        nextOfKinIdNumber = null,
        nextOfKinName = null,
        nextOfKinPhoneNumber = null,
        role = Role(),
        commissionPercentage = null,
        termsAccepted = false
    )
}