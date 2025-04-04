package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.ClientDTO
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.repos.ClientRepo
import com.bbs.bbsapi.repos.RoleRepository
import com.bbs.bbsapi.repos.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class ClientService(
    private val clientRepository: ClientRepo,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val roleRepository: RoleRepository,
    private val userService: UserService
) {

    fun registerClient(clientDTO: ClientDTO): Client{

        val clientRole = roleRepository.findByName("CLIENT")
            .orElseThrow { RuntimeException("CLIENT role not found") }
        val user = User(
            username = clientDTO.firstName, // or email/phone depending on your login strategy
            password = passwordEncoder.encode("defaultPassword123"), // Replace with actual generated or provided password
            email = clientDTO.email.toString(),
            phonenumber = clientDTO.phoneNumber,
            role = clientRole
        )
        userRepository.save(user)
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
            country = clientDTO.country.toString(),
            county = clientDTO.county,
            countryCode = clientDTO.countryCode,
            idNumber = clientDTO.idNumber,
            clientStage = ClientStage.REGISTRATION
        )
        clientRepository.save(client)



        userService.generateRegisterToken(user)
        return client
    }

    fun getClientById(clientId: Long): Client {
        return clientRepository.findById(clientId)
            .orElseThrow { RuntimeException("Client not found") }
    }

    fun getAllClients(): List<Client> {
        return clientRepository.findAll()
    }

    fun updateClient(clientDTO: ClientDTO): Client? {
        val client = clientDTO.id?.let { clientRepository.findById(it)
            .orElseThrow { IllegalArgumentException("CLIENT does not exist") }
        }
        client?.country = clientDTO.country.toString()
        client?.email = clientDTO.email
        client?.firstName = clientDTO.firstName
        client?.secondName = clientDTO.secondName
        client?.email = clientDTO.email
        client?.lastName = clientDTO.surName
        client?.county = clientDTO.county
        client?.dob = clientDTO.dob
        client?.gender = clientDTO.gender
        client?.idNumber = clientDTO.idNumber
        client?.location = clientDTO.locationType
        val updatedClient = client?.let { clientRepository.save(it) }
        return updatedClient

    }
}
