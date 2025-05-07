package com.bbs.bbsapi.models

import jakarta.persistence.Entity
import jakarta.persistence.*

@Entity
@Table(name = "preliminary_type")
data class PreliminaryType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String? = "",

    @Column
    val description: String? = "",

    @Column(name = "requires_government_approval", nullable = false)
    val requiresGovernmentApproval: Boolean = false
)