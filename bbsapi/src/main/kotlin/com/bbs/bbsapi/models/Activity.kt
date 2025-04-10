package com.bbs.bbsapi.models

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "activities")
data class Activity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val clientId: Long, // Or whatever ID represents the client
    val description: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val user: String? = null // Optional: User who performed the action
){
    constructor() : this(
        id = 0,
        clientId = 0,
        description = "",
        timestamp = LocalDateTime.now(),
        user = null
    )
}