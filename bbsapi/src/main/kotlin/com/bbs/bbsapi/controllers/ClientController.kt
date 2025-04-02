package com.bbs.bbsapi.controllers


import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.entities.ClientDTO
import com.bbs.bbsapi.repos.ClientRepo
import com.bbs.bbsapi.services.ClientService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clients")
class ClientController(private val clientService: ClientService, private val clientRepo: ClientRepo) {

    @PostMapping("/register")
    fun createClient(@RequestBody clientDTO: ClientDTO): ResponseEntity<Client> {
        val client = Client(
            firstName = clientDTO.firstName,
            secondName = clientDTO.secondName,
            lastName = clientDTO.surName,
            email = clientDTO.email,
            phoneNumber = clientDTO.phoneNumber,
            dob = clientDTO.dob,
            gender = clientDTO.gender,
            preferredContact = clientDTO.preferredContact,
            location = clientDTO.locationType,
            country = clientDTO.country,
            county = clientDTO.county,
            countryCode = clientDTO.countryCode,
            idNumber = clientDTO.idNumber
        )
        return ResponseEntity.ok(clientRepo.save(client))
    }


    @GetMapping("/{id}")
    fun getClientById(@PathVariable id: Long): ResponseEntity<Client> {
        val client = clientService.getClientById(id)
        return ResponseEntity.ok(client)
    }

    @GetMapping
    fun getAllClients(): ResponseEntity<List<Client>> {
        val clients = clientService.getAllClients()
        return ResponseEntity.ok(clients)
    }
}
