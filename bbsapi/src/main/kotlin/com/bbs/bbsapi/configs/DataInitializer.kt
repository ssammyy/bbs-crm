package com.bbs.bbsapi.configs

import com.bbs.bbsapi.models.BaseEntity
import com.bbs.bbsapi.models.Privilege
import com.bbs.bbsapi.models.Role
import com.bbs.bbsapi.repos.PrivilegeRepository
import com.bbs.bbsapi.repos.RoleRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataInitializer(
    private val roleRepository: RoleRepository,
    private val privilegeRepository: PrivilegeRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        initializePrivilegesAndRoles()
    }

    @Transactional
    fun initializePrivilegesAndRoles() {
        val privileges = listOf(
            "CREATE_USER", "DELETE_USER", "VIEW_REPORTS",
            "MANAGE_PROJECTS", "EDIT_PROFILE", "UPLOAD_DOCUMENTS"
        ).map { privilegeRepository.findByName(it).orElseGet { privilegeRepository.save(Privilege(name = it)) } }
            .toSet()

        val roles = mapOf(
            "Super Admin" to privileges,
            "Sales Director" to privileges.filter { it.name in listOf("VIEW_REPORTS", "MANAGE_PROJECTS") }.toSet(),
            "Sales Rep" to privileges.filter { it.name in listOf("EDIT_PROFILE") }.toSet(),
            "Technical Director" to privileges.filter { it.name in listOf("MANAGE_PROJECTS") }.toSet(),
            "Architect" to privileges.filter { it.name in listOf("UPLOAD_DOCUMENTS") }.toSet(),
            "Client" to emptySet<Privilege>()
        )

        roles.forEach { (roleName, rolePrivileges) ->
            roleRepository.findByName(roleName).orElseGet {
                roleRepository.save(Role(name = roleName, privileges = rolePrivileges.toMutableSet()))
            }
        }
    }
}
