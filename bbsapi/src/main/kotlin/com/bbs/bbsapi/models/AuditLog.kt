package com.bbs.bbsapi.models

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
data class AuditLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val reportId: Long,
    val changedBy: String,
    val fieldName: String,
    val oldValue: String?,
    val newValue: String?,
    val changeDate: LocalDateTime = LocalDateTime.now()
)