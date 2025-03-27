package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.UserDTO
import com.bbs.bbsapi.security.CustomUserDetails
import com.bbs.bbsapi.services.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    fun getAuthenticatedUser(@AuthenticationPrincipal userDetails: CustomUserDetails): UserDTO {
        return userService.getUserDetails(userDetails.username)
    }
}
