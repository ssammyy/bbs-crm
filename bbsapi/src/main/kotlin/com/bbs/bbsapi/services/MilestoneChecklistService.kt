package com.bbs.bbsapi.services

import com.bbs.bbsapi.dtos.MilestoneChecklistDTO
import com.bbs.bbsapi.dtos.MilestoneItemDTO
import com.bbs.bbsapi.models.Milestone
import com.bbs.bbsapi.repositories.ClientRepository
import com.bbs.bbsapi.repositories.MilestoneRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MilestoneChecklistService(
    private val milestoneRepository: MilestoneRepository,
    private val clientRepository: ClientRepository
) {
    private val defaultMilestones = listOf(
        "Change of use/user.",
        "Designing architectural drawings.",
        "Geotechnical analysis report.",
        "Approval of architectural drawings by the county government.",
        "Preparing structural drawings.",
        "Approval of structural drawings by the county government.",
        "Preparation of bills of quantities.",
        "Contract signing.",
        "NEMA permit.",
        "NCA project registration.",
        "Site handover.",
        "Mobilization.",
        "Groundbreaking.",
        "Construction process.",
        "Completion and final inspection.",
        "Handover."
    )

    @Transactional
    fun getChecklistForClient(clientId: Long): ResponseEntity<MilestoneChecklistDTO> {
        // Get client details
        val client = clientRepository.findById(clientId)
            .orElseThrow { IllegalArgumentException("Client not found") }

        // Get existing milestones or create default ones
        var milestones = milestoneRepository.findByClientIdOrderByOrderAsc(clientId)
        
        if (milestones.isEmpty()) {
            // Create default milestones for new client
            milestones = defaultMilestones.mapIndexed { index, name ->
                Milestone(
                    clientId = clientId,
                    name = name,
                    order = index
                )
            }.also { milestoneRepository.saveAll(it) }
        }

        // Convert to DTO
        val milestoneItems = milestones.map { milestone ->
            milestone.name?.let {
                MilestoneItemDTO(
                    name = it,
                    completed = milestone.completed
                )
            }
        }

        return ResponseEntity.ok(MilestoneChecklistDTO(
            clientId = clientId,
            clientName = "${client.firstName} ${client.lastName}",
            siteLocation = client.projectName,
            milestones = milestoneItems
        ))
    }

    @Transactional
    fun updateMilestoneStatus(clientId: Long, milestoneName: String, completed: Boolean): ResponseEntity<MilestoneItemDTO> {
        val milestone = milestoneRepository.findByClientIdOrderByOrderAsc(clientId)
            .find { it.name == milestoneName }
            ?: throw IllegalArgumentException("Milestone not found")

        milestone.completed = completed
        milestone.completedAt = if (completed) java.time.LocalDateTime.now() else null
        milestoneRepository.save(milestone)

        return ResponseEntity.ok(milestone.name?.let {
            MilestoneItemDTO(
                name = it,
                completed = milestone.completed
            )
        })
    }
} 