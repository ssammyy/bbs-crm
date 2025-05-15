package com.bbs.bbsapi.entities

import java.time.LocalDateTime

data class RoleDTO(
    val id: Long? = null,
    val name: String? = null,
    val privilegeIds: List<Long> = emptyList(),
    val privileges: List<PrivilegeDTO>? = null
)

data class PrivilegeDTO(
    val name: String,
    val id: Long? = null,
    val createdOn: LocalDateTime? = null,
    val createdBy: String? = null,
    val updatedOn: LocalDateTime? = null,
    val updatedBy: String? = null,
)

data class UserDTO(
    val id: Long? = null,
    val username: String,
    val email: String,
    val password: String? = null,
    val phonenumber: String,
    val paymentMethod: String? = null,
    val bankName: String? = null,
    val bankAccountNumber: String? = null,
    val bankBranch: String? = null,
    val bankAccountHolderName: String? = null,
    val role: RoleDTO
)

data class UserRegeDTO(
    val username: String,
    val email: String,
    val phonenumber: String,
    val paymentMethod: String? = null,
    val bankName: String? = null,
    val bankAccountNumber: String? = null,
    val bankBranch: String? = null,
    val bankAccountHolderName: String? = null,
    val roleId: Long
)

data class SetPasswordRequest(
    val newPassword: String
)