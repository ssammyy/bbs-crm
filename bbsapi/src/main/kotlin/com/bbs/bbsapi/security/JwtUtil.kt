package com.bbs.bbsapi.security

import com.bbs.bbsapi.models.User
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor("your_very_secret_key_must_be_at_least_32_chars".toByteArray())

    fun generateToken(user: User): String {
        val privileges = user.role?.privileges?.map { it.name }
        return Jwts.builder()
            .subject(user.username)
            .claim("userId", user.id)
            .claim("role", user.role?.name)
            .claim("privileges", privileges)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1-hour expiry
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String? {
        return extractClaims(token)?.subject
    }

    fun extractClaims(token: String): Claims? {
        return try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).payload
        } catch (e: Exception) {
            null
        }
    }

    fun isTokenValid(token: String, username: String): Boolean {
        val extractedUsername = extractUsername(token)
        return extractedUsername == username && !isTokenExpired(token)
    }
    fun validateToken(token: String, userDetails: CustomUserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username
    }

    fun getAuthentication(token: String, userDetails: CustomUserDetails): UsernamePasswordAuthenticationToken {
        val authorities: Collection<GrantedAuthority> = userDetails.authorities
        return UsernamePasswordAuthenticationToken(userDetails, null, authorities)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractClaims(token)?.expiration?.before(Date()) ?: true
    }


}
