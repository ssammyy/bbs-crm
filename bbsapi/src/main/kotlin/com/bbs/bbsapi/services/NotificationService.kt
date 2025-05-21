package com.bbs.bbsapi.services

import com.bbs.bbsapi.models.Notification
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.repos.NotificationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    @Transactional
    fun createNotification(user: User, title: String, message: String, type: String, relatedEntityId: Long? = null, relatedEntityType: String? = null): Notification {
        val notification = Notification(
            user = user,
            title = title,
            message = message,
            type = type,
            relatedEntityId = relatedEntityId,
            relatedEntityType = relatedEntityType
        )
        return notificationRepository.save(notification)
    }

    @Transactional(readOnly = true)
    fun getUserNotifications(userId: Long): List<Notification> {
        return notificationRepository.findByUserIdOrderByCreatedOnDesc(userId)
    }

    @Transactional(readOnly = true)
    fun getUnreadNotificationsCount(userId: Long): Long {
        return notificationRepository.countByUserIdAndIsReadFalse(userId)
    }

    @Transactional
    fun markNotificationAsRead(notificationId: Long) {
        notificationRepository.findById(notificationId).ifPresent { notification ->
            notification.isRead = true
            notificationRepository.save(notification)
        }
    }

    @Transactional
    fun markAllNotificationsAsRead(userId: Long) {
        val unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedOnDesc(userId)
        unreadNotifications.forEach { notification ->
            notification.isRead = true
            notificationRepository.save(notification)
        }
    }
} 