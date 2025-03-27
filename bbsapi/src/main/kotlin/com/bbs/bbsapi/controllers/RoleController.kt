package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.RoleDTO
import com.bbs.bbsapi.models.Role
import com.bbs.bbsapi.services.RoleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/roles")
@CrossOrigin("*")
class RoleController(private val roleService: RoleService) {

    @GetMapping
    fun getAllRoles(): ResponseEntity<List<RoleDTO>> {
        return ResponseEntity.ok(roleService.getAllRoles())
    }

    @GetMapping("/{id}")
    fun getRoleById(@PathVariable id: Long): ResponseEntity<Role?> {
        return ResponseEntity.ok(roleService.getRoleById(id))
    }

    @PostMapping
    fun createRole(@RequestBody roleDTO: RoleDTO): ResponseEntity<Role> {
        return ResponseEntity.ok(roleService.createRole(roleDTO))
    }

    @PutMapping("/{id}")
    fun updateRole(@PathVariable id: Long, @RequestBody roleDTO: RoleDTO): ResponseEntity<Role?> {
        val updatedRole = roleService.updateRole(id, roleDTO) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updatedRole)
    }

    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long): ResponseEntity<Void> {
        roleService.deleteRole(id)
        return ResponseEntity.noContent().build()
    }
}
