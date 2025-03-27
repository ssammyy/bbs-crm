package com.bbs.bbsapi.models

import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime

class EntityListener {

    private fun getCurrentUser(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.name ?: "system"
    }
    @PrePersist
    fun onPrePersist(base: BaseEntity) {
        base.createdOn = LocalDateTime.now()
        base.updatedOn = LocalDateTime.now()
        base.createdBy = getCurrentUser()
        base.updatedBy = getCurrentUser()
    }

    @PreUpdate
    fun onPreUpdate(base: BaseEntity) {
        base.updatedOn = LocalDateTime.now()
        base.updatedBy = getCurrentUser()
    }

}