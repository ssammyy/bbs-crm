package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.UserDTO
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.security.CustomUserDetails
import com.bbs.bbsapi.services.UserService

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    fun getAuthenticatedUser(@AuthenticationPrincipal userDetails: CustomUserDetails): UserDTO {
        return userService.getUserDetails(userDetails.username)
    }

    @GetMapping("/all")
    fun getAllUsers(): ResponseEntity<List<UserDTO>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }


    @PutMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: Long,
        @RequestBody updatedUserDTO: UserDTO
    ): ResponseEntity<UserDTO> {
        val updatedUser = userService.updateUser(userId, updatedUserDTO)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Void> {
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }
}


//dop_v1_e0ff53567d7f1f4ed419f21e84a5dfabf3aa95fd66cc3815bbe7b3705f5b745e

//access key - w82j7063F/lTV6S2Cx9Iaqf1Y57O03yBy4sLT9BrK0s