package com.bbs.bbsapi.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "verification_token")
data class VerificationToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val token: String,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    val user: User,

    val expiryDate: LocalDateTime
){
    constructor() : this(null, "", User(), LocalDateTime.now())
}
