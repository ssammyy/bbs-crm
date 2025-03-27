package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.PrivilegeDTO
import com.bbs.bbsapi.entities.RoleDTO
import com.bbs.bbsapi.models.Role
import com.bbs.bbsapi.repos.PrivilegeRepository
import com.bbs.bbsapi.repos.RoleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val privilegeRepository: PrivilegeRepository
) {

    fun getAllRoles(): List<RoleDTO> {
        return roleRepository.findAll().map { role ->
            RoleDTO(
                id= role.id,
                name = role.name,
                privileges =role.privileges?.map { privilege ->
                    PrivilegeDTO(
                        id= privilege.id,
                        name = privilege.name,
                        createdOn = privilege.createdOn,
                        createdBy = privilege.createdBy,
                        updatedOn = privilege.updatedOn,
                        updatedBy = privilege.updatedBy
                    )
                }?: emptyList()
            )
        }
    }

    fun getRoleById(roleId: Long): Role? {
        return roleRepository.findById(roleId).orElse(null)
    }

    @Transactional
    fun createRole(roleDTO: RoleDTO): Role {
        val privileges = privilegeRepository.findAllById(roleDTO.privilegeIds).toMutableSet()
        val role = Role(name = roleDTO.name, privileges = privileges)
        return roleRepository.save(role)
    }

    @Transactional
    fun updateRole(roleId: Long, roleDTO: RoleDTO): Role? {
        val role = roleRepository.findById(roleId).orElse(null) ?: return null
        val privileges = privilegeRepository.findAllById(roleDTO.privilegeIds).toMutableSet()
        role.privileges?.clear()
        role.privileges?.addAll(privileges)
        return roleRepository.save(role)
    }

    @Transactional
    fun deleteRole(roleId: Long) {
        roleRepository.deleteById(roleId)
    }
}
