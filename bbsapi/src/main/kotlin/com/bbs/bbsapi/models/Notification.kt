package com.bbs.bbsapi.models

import jakarta.persistence.*

@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @Column(nullable = false)
    var title: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var message: String? = null,

    @Column(nullable = false)
    var isRead: Boolean = false,

    @Column(nullable = false)
    var type: String? = null, // e.g., "SYSTEM", "INVOICE", "APPROVAL", etc.

    @Column
    var relatedEntityId: Long? = null, // ID of the related entity (e.g., invoice ID, approval ID)

    @Column
    var relatedEntityType: String? = null // Type of the related entity
) : BaseEntity() 