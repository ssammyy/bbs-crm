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
    var user: User,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var message: String,

    @Column(nullable = false)
    var isRead: Boolean = false,

    @Column(nullable = false)
    var type: String, // e.g., "SYSTEM", "INVOICE", "APPROVAL", etc.

    @Column
    var relatedEntityId: Long? = null, // ID of the related entity (e.g., invoice ID, approval ID)

    @Column
    var relatedEntityType: String? = null // Type of the related entity
) : BaseEntity() 