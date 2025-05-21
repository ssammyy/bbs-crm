package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.models.Notification
import com.bbs.bbsapi.services.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {
    @GetMapping
    fun getUserNotifications(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<Notification>> {
        val userId = (userDetails as com.bbs.bbsapi.models.User).id!!
        return ResponseEntity.ok(notificationService.getUserNotifications(userId))
    }

    @GetMapping("/unread/count")
    fun getUnreadNotificationsCount(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<Long> {
        val userId = (userDetails as com.bbs.bbsapi.models.User).id!!
        return ResponseEntity.ok(notificationService.getUnreadNotificationsCount(userId))
    }

    @PostMapping("/{notificationId}/read")
    fun markNotificationAsRead(
        @PathVariable notificationId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        notificationService.markNotificationAsRead(notificationId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/read-all")
    fun markAllNotificationsAsRead(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<Void> {
        val userId = (userDetails as com.bbs.bbsapi.models.User).id!!
        notificationService.markAllNotificationsAsRead(userId)
        return ResponseEntity.ok().build()
    }
} 