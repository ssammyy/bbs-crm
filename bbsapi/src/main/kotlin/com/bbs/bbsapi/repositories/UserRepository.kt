package com.bbs.bbsapi.repositories

import com.bbs.bbsapi.models.Privilege
import com.bbs.bbsapi.models.Role
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.models.VerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun findByName(name: String): Optional<Role>
}
@Repository
interface PrivilegeRepository : JpaRepository<Privilege, Long> {
    fun findByName(name: String): Optional<Privilege>
}
@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByUsernameOrEmail(username: String, email: String): User?
     fun countUserByRole_Name(s: String): Long
     fun countUserByRoleNot(s: Role): Long
     fun findByRole_Name(s: String): List<User>
    fun findFirstByRole_Name(s: String): Optional<User>
}

@Repository
interface TokenRepository : JpaRepository<VerificationToken, Long> {
    fun findByToken(token: String): VerificationToken?
    fun deleteAllByUser_Id(userId: Long)
}


