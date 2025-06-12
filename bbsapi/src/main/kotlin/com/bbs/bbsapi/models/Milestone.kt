package com.bbs.bbsapi.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "milestones")
data class Milestone(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val clientId: Long? = null,

    @Column(nullable = false)
    val name: String? = null,

    @Column(nullable = false)
    var completed: Boolean = false,

    @Column(name = "milestone_order", nullable = false)
    val order: Int? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    var completedAt: LocalDateTime? = null
) 