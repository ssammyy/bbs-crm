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
            "CREATE_USER",
            "DELETE_USER",
            "VIEW_REPORTS",
            "MANAGE_PROJECTS",
            "EDIT_PROFILE",
            "UPLOAD_DOCUMENTS",
            "VIEW_PROGRESS",
            "CREATE_PRIVILEGE",
            "CREATE_ROLE",
            "EDIT_PRIVILEGE",
            "EDIT_ROLE",
            "VIEW_PRIVILEGES",
            "VIEW_ROLES",
            "DELETE_PRIVILEGE",
            "DELETE_ROLE",
            "VIEW_FILES",
            "UPLOAD_FILES",
            "UPDATE_FILES",
            "DELETE_FILES",
            "VIEW_MAIN_DASHBOARD",
            "VIEW_PROJECT_DASHBOARD",
            "VIEW_TECHNICAL_DASHBOARD",
            "VIEW_SALES_DASHBOARD",
            "VIEW_CLIENT_DASHBOARD",
            "VIEW_AGENT_DASHBOARD",
            "VIEW_INVOICES",
            "CREATE_INVOICES",
            "RECONCILE_INVOICES",
            "VIEW_RECEIPTS",
            "UPLOAD_TECHNICAL_WORKS",
            "VIEW_TECHNICAL_WORKS",
            "ONBOARD_CLIENT",
            "VIEW_CLIENTS",
            "EDIT_CLIENT",
            "DELETE_CLIENT",
            "VIEW_ACTIVITY_FEED",
            "CREATE_PRELIMINARY",
            "VIEW_PRELIMINARY",
            "VIEW_LEADS",
            "MANAGE_CLIENTS",
            "MANAGE_USERS",
            "MANAGE_ROLES_PRIVILEGES",
            "MANAGE_CLIENT_DETAILS",
            "MANAGE_INVOICES",
            "MANAGE_CLIENT_ACTIVITY",
            "MANAGE_RECEIPTS",
            "APPROVE_INVOICE",
            "SUBMIT_SITE_VISIT_REPORT",
            "MANAGE_PRELIMINARY",
            "APPROVE_TECHNICAL_WORKS",
            "VIEW_CLIENT_PROFILE",
            "VIEW_AGENT_DASHBOARD",
            "APPROVE_PRELIMINARY",
            "UPLOAD_BOQ"
        ).map { privilegeRepository.findByName(it).orElseGet { privilegeRepository.save(Privilege(name = it)) } }
            .toSet()

        val roles = mapOf(
            "SUPER_ADMIN" to privileges,
            "SALES_DIRECTOR" to privileges.filter {
                it.name in listOf(
                    "MANAGE_RECEIPTS",
                    "MANAGE_PROJECTS",
                    "VIEW_RECEIPTS",
                    "VIEW_INVOICES",
                    "MANAGE_CLIENT",
                    "VIEW_MAIN_DASHBOARD",
                    "VIEW_CLIENT_PROFILE",
                    "VIEW_REPORTS",
                    "VIEW_ACTIVITY_FEED",
                    "MANAGE_CLIENT_DETAILS",
                    "VIEW_CLIENTS",
                    "VIEW_SALES_DASHBOARD",
                    "MANAGE_INVOICES",
                    "VIEW_LEADS",
                    "VIEW_PRELIMINARY",
                    "MANAGE_CLIENT_ACTIVITY"
                )
            }.toSet(),
            "SALES_REP" to privileges.filter {
                it.name in listOf(
                    "MANAGE_PRELIMINARY",
                    "MANAGE_CLIENTS",
                    "VIEW_MAIN_DASHBOARD",
                    "CREATE_PRELIMINARY",
                    "MANAGE_CLIENT_DETAILS",
                    "VIEW_CLIENTS",
                    "ONBOARD_CLIENT",
                    "CREATE_INVOICES",
                    "MANAGE_INVOICES",
                    "VIEW_LEADS",
                    "VIEW_PRELIMINARY",
                    "EDIT_PROFILE",
                    "MANAGE_CLIENT_ACTIVITY"
                )
            }.toSet(),
            "TECHNICAL_DIRECTOR" to privileges.filter {
                it.name in listOf(
                    "MANAGE_PROJECTS",
                    "MANAGE_PRELIMINARY",
                    "VIEW_TECHNICAL_WORKS",
                    "MANAGE_CLIENTS",
                    "VIEW_MAIN_DASHBOARD",
                    "VIEW_CLIENT_PROFILE",
                    "VIEW_ACTIVITY_FEED",
                    "VIEW_CLIENTS",
                    "MANAGE_INVOICES",
                    "VIEW_PRELIMINARY",
                    "MANAGE_CLIENT_ACTIVITY"
                )
            }.toSet(),
            "ARCHITECT" to privileges.filter {
                it.name in listOf(
                    "MANAGE_PROJECTS",
                    "MANAGE_PRELIMINARY",
                    "VIEW_TECHNICAL_WORKS",
                    "MANAGE_CLIENTS",
                    "VIEW_MAIN_DASHBOARD",
                    "VIEW_CLIENT_PROFILE",
                    "VIEW_ACTIVITY_FEED",
                    "VIEW_CLIENTS",
                    "MANAGE_INVOICES",
                    "APPROVE_TECHNICAL_WORKS",
                    "VIEW_PRELIMINARY",
                    "MANAGE_CLIENT_ACTIVITY"
                )
            }.toSet(),
            "CLIENT" to privileges.filter {
                it.name in listOf(
                    "MANAGE_RECEIPTS",
                    "VIEW_CLIENT_DASHBOARD",
                    "MANAGE_CLIENT_DETAILS",
                    "VIEW_INVOICES",
                    "VIEW_CLIENT_PROFILE",
                    "MANAGE_INVOICES",
                    "VIEW_ACTIVITY_FEED",
                    "MANAGE_CLIENT_ACTIVITY"
                ).toSet()
            },
            "AGENT" to privileges.filter {
                it.name in listOf(
                    "MANAGE_RECEIPTS",
                    "VIEW_PROGRESS",
                    "CREATE_INVOICES",
                    "VIEW_AGENT_DASHBOARD"
                )
            }.toSet(),
            "QUALITY_SURVEYOR" to privileges.filter {
                it.name in listOf(
                    "MANAGE_RECEIPTS",
                    "VIEW_PROGRESS",
                    "CREATE_INVOICES",
                    "VIEW_AGENT_DASHBOARD"
                )
            }.toSet(),
            "MANAGING_DIRECTOR" to privileges.filter {
                it.name in listOf(
                    "MANAGE_PROJECTS",
                    "MANAGE_PRELIMINARY",
                    "VIEW_TECHNICAL_WORKS",
                    "MANAGE_CLIENTS",
                    "VIEW_MAIN_DASHBOARD",
                    "VIEW_CLIENT_PROFILE",
                    "VIEW_ACTIVITY_FEED",
                    "VIEW_CLIENTS",
                    "MANAGE_INVOICES",
                    "APPROVE_TECHNICAL_WORKS",
                    "VIEW_PRELIMINARY",
                    "MANAGE_CLIENT_ACTIVITY"
                )
            }
        )

        roles.forEach { (roleName, rolePrivileges) ->
            val existingRole = roleRepository.findByName(roleName)
            if (existingRole.isPresent) {
                // Update existing role with new privileges
                val role = existingRole.get()
                role.privileges?.addAll(rolePrivileges)
                roleRepository.save(role)
            } else {
                // Create new role
                roleRepository.save(Role(name = roleName, privileges = rolePrivileges.toMutableSet()))
            }
        }
    }

    @Transactional
    fun initializePreliminaryTypes() {
        val preliminaryTypes = listOf(
            PreliminaryType(
                name = "ARCHITECTURAL_SKETCHES",
                description = "Initial conceptual designs outlining the project's vision"
            ),
            PreliminaryType(
                name = "ARCHITECTURAL_DRAWINGS",
                description = "Detailed technical drawings including floor plans, elevations, and sections"
            ),
            PreliminaryType(
                name = "BOQ_PREPARATION",
                description = "Bill of Quantities listing materials, labor, and costs"
            ),
            PreliminaryType(
                name = "SITE_SURVEY",
                description = "Topographical and boundary survey of the construction site"
            ),
            PreliminaryType(
                name = "STRUCTURAL_DESIGNS",
                description = "Engineering drawings for the structural framework"
            ),
            PreliminaryType(
                name = "ENVIRONMENTAL_IMPACT_ASSESSMENT",
                description = "Study to assess environmental effects, required for regulatory approval"
            ),
            PreliminaryType(
                name = "GEOTECHNICAL_INVESTIGATION",
                description = "Soil testing to determine foundation requirements"
            ),
            PreliminaryType(
                name = "COST_ESTIMATION",
                description = "Preliminary budgeting based on initial designs and BOQ"
            ),
            PreliminaryType(
                name = "REGULATORY_APPROVALS",
                description = "Documentation for permits from local authorities"
            ),
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