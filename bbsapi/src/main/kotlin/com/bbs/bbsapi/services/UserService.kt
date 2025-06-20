package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.PrivilegeDTO
import com.bbs.bbsapi.entities.RoleDTO
import com.bbs.bbsapi.entities.UserDTO
import com.bbs.bbsapi.entities.UserRegeDTO
import com.bbs.bbsapi.models.*
import com.bbs.bbsapi.repositories.*
import com.bbs.bbsapi.security.CustomUserDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
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
    private val emailService: EmailService,
    private val pendingAgentApprovalRepository: PendingAgentApprovalRepository,
    private val notificationRepository: NotificationRepository
) {
    val logger = LoggerFactory.getLogger(UserService::class.java)

    @Transactional
    fun registerUser(userDTO: UserRegeDTO): Any {
        try {
            logger.info("Registering user: of role id:  ${userDTO.roleId}")
            val role = roleRepository.findById(userDTO.roleId).orElseThrow { IllegalArgumentException("Role could not be found") }
        logger.info("Registering user ${userDTO.username} to ${role?.name}")
            // Check if this is an agent registration
            if (role?.name == "AGENT") {
                // Create pending approval
                val pendingApproval = PendingAgentApproval(
                    username = userDTO.username,
                    email = userDTO.email,
                    phonenumber = userDTO.phoneNumber,
                    paymentMethod = userDTO.paymentMethod,
                    bankName = userDTO.bankName,
                    bankAccountNumber = userDTO.bankAccountNumber,
                    bankBranch = userDTO.bankBranch,
                    bankAccountHolderName = userDTO.bankAccountHolderName,
                    role = role,
                    commissionPercentage = userDTO.percentage
                )
                return pendingAgentApprovalRepository.save(pendingApproval)
            }

            // For non-agent users, proceed with normal registration
            val user = User(
                username = userDTO.username,
                email = userDTO.email,
                password = passwordEncoder.encode("password"),
                phonenumber = userDTO.phoneNumber,
                paymentMethod = userDTO.paymentMethod,
                bankName = userDTO.bankName,
                bankAccountNumber = userDTO.bankAccountNumber,
                bankBranch = userDTO.bankBranch,
                bankAccountHolderName = userDTO.bankAccountHolderName,
                role = role,
                commissionPercentage = userDTO.percentage,
            )
            val savedUser = userRepository.save(user)
            generateRegisterToken(savedUser)
            return savedUser
        } catch (e: Exception) {
            throw Exception( e.message )
        }
    }

    @Transactional
    fun approveAgent(approvalId: Long): User {
        val approval = pendingAgentApprovalRepository.findById(approvalId)
            .orElseThrow { IllegalArgumentException("Pending approval not found") }

        if (approval.status != ApprovalStatus.PENDING) {
            throw IllegalStateException("This approval has already been processed")
        }

        // Create the actual user
        val user = User(
            username = approval.username!!,
            email = approval.email!!,
            password = passwordEncoder.encode("password"),
            phonenumber = approval.phonenumber!!,
            paymentMethod = approval.paymentMethod,
            bankName = approval.bankName,
            bankAccountNumber = approval.bankAccountNumber,
            bankBranch = approval.bankBranch,
            bankAccountHolderName = approval.bankAccountHolderName,
            role = approval.role,
            commissionPercentage = approval.commissionPercentage,
            nextOfKinIdNumber = approval.nextOfKinIdNumber,
            nextOfKinName = approval.nextOfKinName,
            nextOfKinPhoneNumber = approval.nextOfKinPhoneNumber
        )

        val savedUser = userRepository.save(user)
        generateRegisterToken(savedUser)

        // Update approval status
        approval.status = ApprovalStatus.APPROVED
        approval.approvedAt = LocalDateTime.now()
        approval.approvedBy = getLoggedInUser().username
        pendingAgentApprovalRepository.save(approval)

        return savedUser
    }

    @Transactional
    fun rejectAgent(approvalId: Long, reason: String) {
        val approval = pendingAgentApprovalRepository.findById(approvalId)
            .orElseThrow { IllegalArgumentException("Pending approval not found") }

        if (approval.status != ApprovalStatus.PENDING) {
            throw IllegalStateException("This approval has already been processed")
        }

        approval.status = ApprovalStatus.REJECTED
        approval.rejectionReason = reason
        approval.approvedAt = LocalDateTime.now()
        approval.approvedBy = getLoggedInUser().username
        pendingAgentApprovalRepository.save(approval)
    }

    fun getPendingAgentApprovals(): List<PendingAgentApproval> {
        return pendingAgentApprovalRepository.findByStatus(ApprovalStatus.PENDING)
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
        notificationRepository.deleteAllByUserId(userId)
        tokenRepository.deleteAllByUser_Id(userId)
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
            emailService.sendConfirmationEmail(savedUser.email, token, savedUser)
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
            ),
            termsAccepted = user.termsAccepted
        )
    }

    @Transactional
    fun acceptTerms(username: String): UserDTO {
        val user = userRepository.findByUsername(username) ?: throw RuntimeException("User not found")
        user.termsAccepted = true
        val updatedUser = userRepository.save(user)
        return getUserDetails(updatedUser.username)
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