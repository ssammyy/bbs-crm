package com.bbs.bbsapi.configs

import com.bbs.bbsapi.models.PreliminaryType
import com.bbs.bbsapi.models.Privilege
import com.bbs.bbsapi.models.Role
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.repos.PreliminaryTypeRepository
import com.bbs.bbsapi.repos.PrivilegeRepository
import com.bbs.bbsapi.repos.RoleRepository
import com.bbs.bbsapi.repos.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataInitializer(
    private val roleRepository: RoleRepository,
    private val privilegeRepository: PrivilegeRepository,
    private val userRepository: UserRepository,
    private val preliminaryTypeRepository: PreliminaryTypeRepository, // New dependency
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {
    @Value("\${app.super-admin-pass}")
    private val password: String? = null

    @Value("\${app.super-admin-username}")
    private val userName: String? = null

    @Value("\${app.super-admin-email}")
    private val email: String? = null

    @Value("\${app.super-admin-phone}")
    private val phone: String? = null

    override fun run(vararg args: String?) {
        initializePrivilegesAndRoles()
        initializePreliminaryTypes() // New method call
        initializeSuperUser()
    }

    @Transactional
    fun initializePrivilegesAndRoles() {
        val privileges = listOf(
            "CREATE_USER", "DELETE_USER", "VIEW_REPORTS",
            "MANAGE_PROJECTS", "EDIT_PROFILE", "UPLOAD_DOCUMENTS", "VIEW_PROGRESS"
        ).map { privilegeRepository.findByName(it).orElseGet { privilegeRepository.save(Privilege(name = it)) } }
            .toSet()

        val roles = mapOf(
            "SUPER_ADMIN" to privileges,
            "SALES_DIRECTOR" to privileges.filter { it.name in listOf("VIEW_REPORTS", "MANAGE_PROJECTS") }.toSet(),
            "SALES_REP" to privileges.filter { it.name in listOf("EDIT_PROFILE") }.toSet(),
            "TECHNICAL_DIRECTOR" to privileges.filter { it.name in listOf("MANAGE_PROJECTS") }.toSet(),
            "ARCHITECT" to privileges.filter { it.name in listOf("UPLOAD_DOCUMENTS") }.toSet(),
            "CLIENT" to emptySet<Privilege>(),
            "AGENT" to privileges.filter { it.name in listOf("VIEW_PROGRESS") }.toSet(),
        )

        roles.forEach { (roleName, rolePrivileges) ->
            roleRepository.findByName(roleName).orElseGet {
                roleRepository.save(Role(name = roleName, privileges = rolePrivileges.toMutableSet()))
            }
        }
    }

    @Transactional
    fun initializePreliminaryTypes() {
        val preliminaryTypes = listOf(
            PreliminaryType(name = "ARCHITECTURAL_SKETCHES", description = "Initial conceptual designs outlining the project’s vision"),
            PreliminaryType(name = "ARCHITECTURAL_DRAWINGS", description = "Detailed technical drawings including floor plans, elevations, and sections"),
            PreliminaryType(name = "BOQ_PREPARATION", description = "Bill of Quantities listing materials, labor, and costs"),
            PreliminaryType(name = "SITE_SURVEY", description = "Topographical and boundary survey of the construction site"),
            PreliminaryType(name = "STRUCTURAL_DESIGNS", description = "Engineering drawings for the structural framework"),
            PreliminaryType(name = "ENVIRONMENTAL_IMPACT_ASSESSMENT", description = "Study to assess environmental effects, required for regulatory approval"),
            PreliminaryType(name = "GEOTECHNICAL_INVESTIGATION", description = "Soil testing to determine foundation requirements"),
            PreliminaryType(name = "COST_ESTIMATION", description = "Preliminary budgeting based on initial designs and BOQ"),
            PreliminaryType(name = "REGULATORY_APPROVALS", description = "Documentation for permits from local authorities"),
            PreliminaryType(name = "OPEN", description = "Open")
        )

        preliminaryTypes.forEach { type ->
            preliminaryTypeRepository.findByName(type.name!!).orElseGet {
                preliminaryTypeRepository.save(type)
            }
        }
    }

    @Transactional
    fun initializeSuperUser() {
        // Check if a super user already exists to avoid duplicates
        val userAvailable = userRepository.findByUsername(userName!!)
        if (userAvailable == null) {
            val superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow { IllegalStateException("SUPER_ADMIN role not found") }

            val superUser = User(
                username = userName,
                password = passwordEncoder.encode(password),
                email = email!!,
                phonenumber = phone!!,
                role = superAdminRole
            )

            userRepository.save(superUser)
        }
    }
}