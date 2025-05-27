package com.bbs.bbsapi.services

import com.bbs.bbsapi.controllers.UpdateClientCommitmentDTO
import com.bbs.bbsapi.enums.ContactStatus
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.repos.ClientRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ClientCommitmentService(
    private val clientService: ClientService,
    private val userService: UserService,
    private val clientRepository: ClientRepository
) {


    fun getClientCommitmentsToFollowUp(currentDate: LocalDateTime): List<Client> {
        return clientRepository.findByContactStatusNot( ContactStatus.ONBOARDED)
    }
    @Transactional
    fun markAsContacted(clientCommitment: Client) {
        val loggedInUser = userService.getLoggedInUser().username
        clientService.addClientActivity(clientCommitment, "$loggedInUser contacted client ${clientCommitment.firstName}" )

        clientCommitment.contactStatus = ContactStatus.CONTACTED
        clientRepository.save(clientCommitment)
    }

    @Transactional
    fun updateContactStatus(clientCommitmentId: Long, newStatus: ContactStatus): Client? {
        val clientCommitment = clientRepository.findById(clientCommitmentId)
            .orElseThrow { IllegalArgumentException("Client commitment with ID $clientCommitmentId not found") }
        val loggedInUser = userService.getLoggedInUser().username
        if (newStatus == ContactStatus.ONBOARDED) {
            clientService.addClientActivity(clientCommitment, "$loggedInUser changed on-boarded client ${clientCommitment.firstName} ${clientCommitment.lastName}  " )
        }
        clientService.addClientActivity(clientCommitment, "$loggedInUser changed lead ${clientCommitment.firstName} ${clientCommitment.lastName}  status to $newStatus  " )

        clientCommitment.contactStatus = newStatus
        return clientRepository.save(clientCommitment)
    }
    @Transactional
    fun updateClientCommitment(clientCommitmentId: Long, updateDTO: UpdateClientCommitmentDTO): Client? {
        val clientCommitment = clientRepository.findById(clientCommitmentId)
            .orElseThrow { IllegalArgumentException("Client commitment with ID $clientCommitmentId not found") }
        val loggedInUser = userService.getLoggedInUser().username
        if (updateDTO.contactStatus == ContactStatus.ONBOARDED) {
            clientService.addClientActivity(clientCommitment, "$loggedInUser changed on-boarded client ${clientCommitment.firstName} ${clientCommitment.lastName}  " )
        }
        clientService.addClientActivity(clientCommitment, "$loggedInUser changed lead ${clientCommitment.firstName} ${clientCommitment.lastName}  status to $updateDTO.contactStatus  " )

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        clientCommitment.followUpDate = LocalDateTime.parse(updateDTO.followUpDate, formatter)
        clientCommitment.contactStatus = updateDTO.contactStatus
        clientCommitment.notes = updateDTO.notes

        return clientRepository.save(clientCommitment)

    }
}

