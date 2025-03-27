package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.PrivilegeDTO
import com.bbs.bbsapi.models.Privilege
import com.bbs.bbsapi.services.PrivilegeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/privileges")
@CrossOrigin("*")
class PrivilegeController(private val privilegeService: PrivilegeService) {
    @GetMapping
    fun getAllPrivileges(): ResponseEntity<List<Privilege>> {
        return ResponseEntity.ok(privilegeService.getAllPrivileges())
    }

    @GetMapping("/{id}")
    fun getPrivilegeById(@PathVariable id: Long): ResponseEntity<Privilege?> {
        return ResponseEntity.ok(privilegeService.getPrivilegeById(id))
    }

    @PostMapping
    fun createPrivilege(@RequestBody privilegeDTO: PrivilegeDTO): ResponseEntity<Privilege> {
        return ResponseEntity.ok(privilegeService.createPrivilege(privilegeDTO))
    }

    @DeleteMapping("/{id}")
    fun deletePrivilege(@PathVariable id: Long): ResponseEntity<Void> {
        privilegeService.deletePrivilege(id)
        return ResponseEntity.noContent().build()
    }
}
