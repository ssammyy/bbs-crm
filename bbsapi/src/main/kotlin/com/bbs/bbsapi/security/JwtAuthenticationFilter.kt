package com.bbs.bbsapi.security

import com.bbs.bbsapi.services.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: CustomUserDetailsService // ✅ Use CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            try {
                val username = jwtUtil.extractUsername(token)
                if (username != null && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(username) as CustomUserDetails
                    if (jwtUtil.validateToken(token, userDetails)) {
                        val authToken = jwtUtil.getAuthentication(token, userDetails)
                        SecurityContextHolder.getContext().authentication = authToken
                    }
                }
            } catch (e: Exception) {
                println("Invalid or expired token: ${e.message}")
            }
        }

        filterChain.doFilter(request, response)
    }
}
