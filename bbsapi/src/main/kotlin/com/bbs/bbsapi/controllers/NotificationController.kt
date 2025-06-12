package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.models.Notification
import com.bbs.bbsapi.repositories.UserRepository
import com.bbs.bbsapi.services.NotificationService
import com.bbs.bbsapi.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val userService: UserService,
    private val userRepository: UserRepository
) {
    @GetMapping
    fun getUserNotifications(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<Notification>> {
        val loggedInUser = userService.getLoggedInUser().username
        val user = userRepository.findByUsername(loggedInUser)

        return ResponseEntity.ok(notificationService.getUserNotifications(user?.id!!))
    }

    @GetMapping("/unread/count")
    fun getUnreadNotificationsCount(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<Long> {
        val loggedInUser = userService.getLoggedInUser().username
        val user = userRepository.findByUsername(loggedInUser)
        return ResponseEntity.ok(notificationService.getUnreadNotificationsCount(user?.id!!))
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
        val loggedInUser = userService.getLoggedInUser().username
        val user = userRepository.findByUsername(loggedInUser)
        notificationService.markAllNotificationsAsRead(user?.id!!)
        return ResponseEntity.ok().build()
    }
} 