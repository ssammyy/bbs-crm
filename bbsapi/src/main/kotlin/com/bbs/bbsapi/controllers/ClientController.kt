package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.entities.ClientDTO
import com.bbs.bbsapi.entities.UpdateStageRequest
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.models.Activity
import com.bbs.bbsapi.repos.ClientRepository
import com.bbs.bbsapi.services.ClientService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clients")
class ClientController(private val clientService: ClientService, private val clientRepository: ClientRepository) {

    @PostMapping("/register")
    fun createClient(@RequestBody clientDTO: ClientDTO): ResponseEntity<Client> {
        return ResponseEntity.ok(clientService.registerClient(clientDTO))
    }

    @GetMapping("/{id}")
    fun getClientById(@PathVariable id: Long): ResponseEntity<Client> {
        val client = clientService.getClientById(id)
        return ResponseEntity.ok(client)
    }

    @GetMapping("/leads")
    fun getLeads(): ResponseEntity<List<Client>> {
        val clients = clientService.getLeads()
        return ResponseEntity.ok(clients)
    }

    @GetMapping("/active")
    fun getActiveClients(): ResponseEntity<List<Client>> {
        val clients = clientService.getOnBoardedClients()
        return ResponseEntity.ok(clients)
    }

    @GetMapping
    fun getAllClients(): ResponseEntity<List<Client>> {
        val clients = clientService.getAllClients()
        return ResponseEntity.ok(clients)
    }

    @PutMapping("/update")
    fun updateClient(@RequestBody clientDTO: ClientDTO): ResponseEntity<Client> {
        val updatedClient = clientService.updateClient(clientDTO)
        return ResponseEntity.ok(updatedClient!!)
    }

    @GetMapping("/{clientId}/activities")
    fun getClientActivities(@PathVariable clientId: Long): ResponseEntity<List<Activity>> {
        return ResponseEntity.ok(clientService.getClientActivities(clientId))
    }

    @GetMapping("/{clientEmail}/details")
    fun getClientActivities(@PathVariable clientEmail: String): ResponseEntity<Client> {
        return ResponseEntity.ok(clientService.getClientByEmail(clientEmail)!!)
    }

    @PostMapping("/{id}/update-stage")
    fun updateClientStage(
        @PathVariable id: Long,
        @RequestBody request: UpdateStageRequest
    ): ResponseEntity<Client> {
        val client = clientService.getClientById(id)
        clientService.changeClientStatus(
            ClientStage.valueOf(request.currentStage),
            client,
            ClientStage.valueOf(request.newStage),
            request.message
        )
        return ResponseEntity.ok(client)
    }

    @DeleteMapping("/{id}")
    fun softDeleteClient(@PathVariable id: Long): ResponseEntity<Client> {
        val client = clientService.softDeleteClient(id)
        return ResponseEntity.ok(client)
    }
}