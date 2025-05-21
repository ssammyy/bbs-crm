package com.bbs.bbsapi.models

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
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
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
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