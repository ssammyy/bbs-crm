package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.dtos.MilestoneChecklistDTO
import com.bbs.bbsapi.dtos.MilestoneItemDTO
import com.bbs.bbsapi.services.MilestoneChecklistService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clients")
class MilestoneChecklistController(
    private val milestoneChecklistService: MilestoneChecklistService
) {
    @GetMapping("/{clientId}/milestone-checklist")
    fun getMilestoneChecklist(@PathVariable clientId: Long): ResponseEntity<MilestoneChecklistDTO>  {
        return milestoneChecklistService.getChecklistForClient(clientId)
    }

    @PutMapping("/{clientId}/milestone-checklist/{milestoneName}")
    fun updateMilestoneStatus(
        @PathVariable clientId: Long,
        @PathVariable milestoneName: String,
        @RequestParam completed: Boolean
    ): ResponseEntity<MilestoneItemDTO> {
        return milestoneChecklistService.updateMilestoneStatus(clientId, milestoneName, completed)
    }
} 