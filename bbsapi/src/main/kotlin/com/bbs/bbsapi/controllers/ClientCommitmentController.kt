package com.bbs.bbsapi.controllers


import com.bbs.bbsapi.entities.ClientCommitmentDTO
import com.bbs.bbsapi.enums.ContactStatus
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.ClientCommitment
import com.bbs.bbsapi.services.ClientCommitmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/client-commitments")
class ClientCommitmentController(
    private val clientCommitmentService: ClientCommitmentService
) {

//    @PostMapping("/onboard")
//    fun onboardClientCommitment(@RequestBody dto: ClientCommitmentDTO): ResponseEntity<Client> {
//        val clientCommitment = clientCommitmentService.onboardClientCommitment(dto)
//        return ResponseEntity.ok(clientCommitment)
//    }

    @GetMapping("/follow-ups")
    fun getClientCommitmentsToFollowUp(): ResponseEntity<List<Client>> {
        val clientCommitments = clientCommitmentService.getClientCommitmentsToFollowUp(LocalDateTime.now())
        return ResponseEntity.ok(clientCommitments)
    }

    @PatchMapping("/{id}/status")
    fun updateContactStatus(
        @PathVariable id: Long,
        @RequestParam status: ContactStatus
    ): ResponseEntity<Client> {
        val updatedClientCommitment = clientCommitmentService.updateContactStatus(id, status)
        return ResponseEntity.ok(updatedClientCommitment)
    }

    @PutMapping("/{id}")
    fun updateClientCommitment(
        @PathVariable id: Long,
        @RequestBody updateDTO: UpdateClientCommitmentDTO
    ): ResponseEntity<Client> {
        val updatedClientCommitment = clientCommitmentService.updateClientCommitment(id, updateDTO)
        return ResponseEntity.ok(updatedClientCommitment)
    }
}

data class UpdateClientCommitmentDTO(
    val followUpDate: String,
    val contactStatus: ContactStatus,
    val notes: String?
)