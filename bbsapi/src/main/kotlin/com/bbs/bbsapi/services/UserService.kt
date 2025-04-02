package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.PrivilegeDTO
import com.bbs.bbsapi.entities.RoleDTO
import com.bbs.bbsapi.entities.UserDTO
import com.bbs.bbsapi.entities.UserRegeDTO
import com.bbs.bbsapi.models.Role
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.repos.RoleRepository
import com.bbs.bbsapi.repos.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.Throws
import kotlin.math.log


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val roleRepository: RoleRepository
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
                role = role
            )
            return userRepository.save(user)

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
