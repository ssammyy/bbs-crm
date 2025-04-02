package com.bbs.bbsapi.services

import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.repos.ClientRepo
import org.springframework.stereotype.Service

@Service
class ClientService(private val clientRepository: ClientRepo) {

    fun registerClient(client: Client): Client {
        return clientRepository.save(client)
    }

    fun getClientById(clientId: Long): Client {
        return clientRepository.findById(clientId)
            .orElseThrow { RuntimeException("com.bbs.bbsapi.models.Client not found") }
    }

    fun getAllClients(): List<Client> {
        return clientRepository.findAll()
    }
}
