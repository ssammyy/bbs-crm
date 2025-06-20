package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.UserDTO
import com.bbs.bbsapi.models.Role
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.security.CustomUserDetails
import com.bbs.bbsapi.services.UserService

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.CacheEvict

@RestController
@RequestMapping("/api/user")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    fun getAuthenticatedUser(@AuthenticationPrincipal userDetails: CustomUserDetails): UserDTO {
        return userService.getUserDetails(userDetails.username)
    }

    @GetMapping("/all")
    @Cacheable(value = ["users"], unless = "#result == null")
    fun getAllUsers(): ResponseEntity<List<UserDTO>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }


    @PutMapping("/{userId}")
    @CacheEvict(value = ["users"], allEntries = true)
    fun updateUser(
        @PathVariable userId: Long,
        @RequestBody updatedUserDTO: UserDTO
    ): ResponseEntity<UserDTO> {
        val updatedUser = userService.updateUser(userId, updatedUserDTO)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{userId}")
    @CacheEvict(value = ["users"], allEntries = true)
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Void> {
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/agents")
    fun getAgents(): ResponseEntity<List<User>> {
        val agents = userService.getAllUsers()
            .filter { it.role.name == "AGENT" }
            .map { User(it.id, it.username, "", it.email, it.phonenumber) }
        return ResponseEntity.ok(agents)
    }

    @PostMapping("/accept-terms")
    fun acceptTerms(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<UserDTO> {
        val updatedUser = userService.acceptTerms(userDetails.username)
        return ResponseEntity.ok(updatedUser)
    }
}


//dop_v1_e0ff53567d7f1f4ed419f21e84a5dfabf3aa95fd66cc3815bbe7b3705f5b745e

//access key - w82j7063F/lTV6S2Cx9Iaqf1Y57O03yBy4sLT9BrK0s