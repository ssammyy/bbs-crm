package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.SetPasswordRequest
import com.bbs.bbsapi.entities.UserRegeDTO
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.models.VerificationToken
import com.bbs.bbsapi.repos.TokenRepository
import com.bbs.bbsapi.repos.UserRepository
import com.bbs.bbsapi.security.JwtUtil
import com.bbs.bbsapi.services.EmailService
import com.bbs.bbsapi.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder
) {

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

    @PostMapping("/request-password-reset")
    fun requestReset(@RequestParam email: String): ResponseEntity<String> {
        val user = userRepository.findByEmail(email)
            ?: return ResponseEntity.badRequest().body("No user with this email")

        val token = UUID.randomUUID().toString()
        val expiry = LocalDateTime.now().plusHours(1)

        val resetToken = VerificationToken(
            token = token,
            user = user,
            expiryDate = expiry
        )

        tokenRepository.save(resetToken)

        emailService.sendResetPasswordEmail(email, token)

        return ResponseEntity.ok("Reset link sent to email")
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestParam token: String, @RequestParam newPassword: String): ResponseEntity<String> {
        val resetToken = tokenRepository.findByToken(token)
            ?: return ResponseEntity.badRequest().body("Invalid or expired token")

        if (resetToken.expiryDate.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired")
        }

        val user = resetToken.user
        user.password = passwordEncoder.encode(newPassword)
        userRepository.save(user)

        return ResponseEntity.ok("Password reset successful")
    }

//            TODO("CHECK COMMENTED CODE ")

    @PostMapping("/confirm-email")
    fun confirmEmail(@RequestBody request: Map<String, String>): ResponseEntity<*> {
        val token = request["token"] ?: return ResponseEntity.badRequest().body("Missing token")
        val emailToken = tokenRepository.findByToken(token)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid token")
//        if (emailToken.confirmed) {
//            return ResponseEntity.badRequest().body("Token already used")
//        }

        if (emailToken.expiryDate.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired")
        }

        val user = emailToken.user
//        user.enabled = true // You'll need to add `enabled` flag to `User`
//        emailToken.confirmed = true

//        userRepository.save(user)
        tokenRepository.save(emailToken)

        return ResponseEntity.ok(emailToken)

    }

    @PutMapping("/set-password")
    fun setPassword(
        @RequestParam token: String,
        @RequestBody request: SetPasswordRequest
    ): ResponseEntity<*> {
        val verificationToken = tokenRepository.findByToken(token)
            ?: return ResponseEntity.badRequest().body("Invalid or expired token")

        val user = verificationToken.user
        user.password = passwordEncoder.encode(request.newPassword)
//        user.enabled = true
        userRepository.save(user)

        // Optionally: delete the token or mark it used
        return ResponseEntity.ok(user)
    }

}





