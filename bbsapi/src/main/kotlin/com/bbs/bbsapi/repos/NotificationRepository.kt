package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserIdOrderByCreatedOnDesc(userId: Long): List<Notification>
    fun countByUserIdAndIsReadFalse(userId: Long): Long
    fun findByUserIdAndIsReadFalseOrderByCreatedOnDesc(userId: Long): List<Notification>
} 