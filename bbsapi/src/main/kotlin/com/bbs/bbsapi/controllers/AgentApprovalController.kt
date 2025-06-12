package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.models.PendingAgentApproval
import com.bbs.bbsapi.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/agent-approvals")
class AgentApprovalController(private val userService: UserService) {

    @GetMapping("/pending")
    fun getPendingApprovals(): ResponseEntity<List<PendingAgentApproval>> {
        return ResponseEntity.ok(userService.getPendingAgentApprovals())
    }

    @PostMapping("/{approvalId}/approve")
    fun approveAgent(@PathVariable approvalId: Long): ResponseEntity<Any> {
        return try {
            val user = userService.approveAgent(approvalId)
            ResponseEntity.ok(user)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "Failed to approve agent")))
        }
    }

    @PostMapping("/{approvalId}/reject")
    fun rejectAgent(
        @PathVariable approvalId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Any> {
        val reason = request["reason"] ?: return ResponseEntity.badRequest().body("Rejection reason is required")
        return try {
            userService.rejectAgent(approvalId, reason)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "Failed to reject agent")))
        }
    }
} 