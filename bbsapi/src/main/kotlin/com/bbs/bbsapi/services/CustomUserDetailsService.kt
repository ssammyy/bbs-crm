package com.bbs.bbsapi.services

import com.bbs.bbsapi.repositories.UserRepository
import com.bbs.bbsapi.security.CustomUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw  UsernameNotFoundException("User $username not found ")
        return CustomUserDetails(user)
    }
}
