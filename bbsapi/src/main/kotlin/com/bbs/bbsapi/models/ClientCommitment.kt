package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.ContactStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "client_commitment")
data class ClientCommitment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String ="",

    @Column(nullable = false)
    val email: String = "",

    @Column(nullable = false)
    val phone: String ="",

    @Column
    var notes: String? = null,

    @Column(nullable = false)
    var followUpDate: LocalDateTime =LocalDateTime.now(),

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var updatedAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var contactStatus: ContactStatus = ContactStatus.LEAD
)