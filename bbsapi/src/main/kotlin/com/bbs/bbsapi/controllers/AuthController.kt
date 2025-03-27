package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.UserDTO
import com.bbs.bbsapi.entities.UserRegeDTO
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.security.JwtUtil
import com.bbs.bbsapi.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val userService: UserService, private val jwtUtil: JwtUtil) {

    @PostMapping("/register")
    fun register(@RequestBody user: UserRegeDTO): ResponseEntity<Map<String, String>> {
        return try {
            userService.registerUser(user)
            ResponseEntity.ok(mapOf("message" to "User registered successfully"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to (e.message ?: "Registration failed")))
        }
    }



    @PostMapping("/login")
    fun login(@RequestBody loginRequest: Map<String, String>): ResponseEntity<Any> {
        val username = loginRequest["username"] ?: return ResponseEntity.badRequest().body("Username is required")
        val password = loginRequest["password"] ?: return ResponseEntity.badRequest().body("Password is required")

        val user = userService.findByUsername(username) ?: return ResponseEntity.status(401).body("User not found")
        if (!userService.validatePassword(password, user.password)) {
            return ResponseEntity.status(401).body("Invalid credentials")
        }


        val token = jwtUtil.generateToken(user)
        return ResponseEntity.ok(mapOf("token" to token))
    }
}
