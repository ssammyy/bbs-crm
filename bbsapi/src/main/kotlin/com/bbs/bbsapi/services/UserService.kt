package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.PrivilegeDTO
import com.bbs.bbsapi.entities.RoleDTO
import com.bbs.bbsapi.entities.UserDTO
import com.bbs.bbsapi.entities.UserRegeDTO
import com.bbs.bbsapi.models.Role
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.models.VerificationToken
import com.bbs.bbsapi.repos.RoleRepository
import com.bbs.bbsapi.repos.TokenRepository
import com.bbs.bbsapi.repos.UserRepository
import com.bbs.bbsapi.security.CustomUserDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val roleRepository: RoleRepository,
    private val tokenRepository: TokenRepository,
    private val emailService: EmailService
) {
    @Transactional
    fun registerUser(userDTO: UserRegeDTO): User {
        try {
            val role: Role? = roleRepository.findById(userDTO.roleId).orElse(null)
            val user = User(
                username = userDTO.username,
                email = userDTO.email,
                password = passwordEncoder.encode("password"),
                phonenumber = userDTO.phonenumber,
                paymentMethod = userDTO.paymentMethod,
                bankName = userDTO.bankName,
                bankAccountNumber = userDTO.bankAccountNumber,
                bankBranch = userDTO.bankBranch,
                bankAccountHolderName = userDTO.bankAccountHolderName,
                role = role
            )
            val savedUser = userRepository.save(user)
            generateRegisterToken(savedUser)
            return savedUser
        } catch (e: Exception) {
            throw Exception("Error registering user", e)
        }
    }

    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)

    }

    fun getAllUsers(): List<UserDTO> {
        return userRepository.findAll().map { user ->
            UserDTO(
                id = user.id,
                username = user.username,
                email = user.email,
                password = passwordEncoder.encode("password"),
                phonenumber = user.phonenumber,
                paymentMethod = user.paymentMethod,
                bankName = user.bankName,
                bankAccountNumber = user.bankAccountNumber,
                bankBranch = user.bankBranch,
                bankAccountHolderName = user.bankAccountHolderName,
                role = RoleDTO(
                    id = user.role?.id,
                    name = user.role?.name,
                    privileges = user.role?.privileges?.map {
                        PrivilegeDTO(
                            it.name,
                            it.id,
                            it.createdOn,
                            it.createdBy,
                            it.updatedOn
                        )
                    } ?: emptyList(),
                )
            )
        }
    }

    @Transactional
    fun updateUser(userId: Long, updatedUserDTO: UserDTO): UserDTO {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with ID: $userId") }

        user.username = updatedUserDTO.username
        user.email = updatedUserDTO.email
        user.phonenumber = updatedUserDTO.phonenumber
        user.paymentMethod = updatedUserDTO.paymentMethod
        user.bankName = updatedUserDTO.bankName
        user.bankAccountNumber = updatedUserDTO.bankAccountNumber
        user.bankBranch = updatedUserDTO.bankBranch
        user.bankAccountHolderName = updatedUserDTO.bankAccountHolderName
        user.role = updatedUserDTO.role.id?.let {
            roleRepository.findById(it)
                .orElseThrow { IllegalArgumentException("Role not found with ID: ${updatedUserDTO.role.id}") }
        }

        val updatedUser = userRepository.save(user)
        return UserDTO(
            username = updatedUser.username,
            email = updatedUser.email,
            phonenumber = updatedUser.phonenumber,
            password = passwordEncoder.encode("password"),
            paymentMethod = updatedUser.paymentMethod,
            bankName = updatedUser.bankName,
            bankAccountNumber = updatedUser.bankAccountNumber,
            bankBranch = updatedUser.bankBranch,
            bankAccountHolderName = updatedUser.bankAccountHolderName,
            role = RoleDTO(
                id = updatedUser.role?.id,
                name = updatedUser.role?.name,
                privileges = updatedUser.role?.privileges?.map {
                    PrivilegeDTO(it.name, it.id, it.createdOn, it.createdBy, it.updatedOn)
                } ?: emptyList()
            )
        )
    }

    @Transactional
    fun deleteUser(userId: Long) {
        userRepository.deleteById(userId)
    }

    fun generateRegisterToken(savedUser: User) {
        val token = UUID.randomUUID().toString()
        val expiry = LocalDateTime.now().plusHours(24)

        val verificationToken = VerificationToken(
            token = token,
            user = savedUser,
            expiryDate = expiry
        )

        tokenRepository.save(verificationToken)

        // Send email
        CoroutineScope(Dispatchers.IO).launch {
            emailService.sendConfirmationEmail(savedUser.email, token)
        }
    }

    fun validatePassword(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }

    fun getUserDetails(username: String): UserDTO {
        val user = userRepository.findByUsername(username) ?: throw RuntimeException("User not found")

        return UserDTO(
            id = user.id,
            username = user.username,
            email = user.email,
            password = passwordEncoder.encode(user.password),
            phonenumber = user.phonenumber,
            paymentMethod = user.paymentMethod,
            bankName = user.bankName,
            bankAccountNumber = user.bankAccountNumber,
            bankBranch = user.bankBranch,
            bankAccountHolderName = user.bankAccountHolderName,
            role = RoleDTO(
                id = user.role?.id,
                name = user.role?.name,
                privileges = user.role?.privileges?.map {
                    PrivilegeDTO(
                        it.name,
                        it.id,
                        it.createdOn,
                        it.createdBy,
                        it.updatedOn
                    )
                } ?: emptyList(),
            )
        )
    }

    fun getLoggedInUser(): CustomUserDetails {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.principal as CustomUserDetails
    }

    fun getActualUSer(): User{
        val userName = getLoggedInUser().username
        return userRepository.findByUsername(userName) ?: throw RuntimeException("User not found")
    }
}