package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.ClientDTO
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.models.Activity
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.repos.ActivityRepository
import com.bbs.bbsapi.repos.ClientRepo
import com.bbs.bbsapi.repos.RoleRepository
import com.bbs.bbsapi.repos.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ClientService(
    private val clientRepository: ClientRepo,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val roleRepository: RoleRepository,
    private val userService: UserService,
    private val activityRepository: ActivityRepository
) {
    @Transactional
    fun registerClient(clientDTO: ClientDTO): Client {

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
            clientStage = ClientStage.REQUIREMENTS_GATHERING,
            nextStage = ClientStage.PROFORMA_INVOICE_GENERATION,
            clientSource = clientDTO.clientSource,
            projectName = clientDTO.projectName,
            projectActive = false,
        )
        clientRepository.save(client)
        addClientActivity(client, "Registration on the system")

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
        val client = clientDTO.id?.let {
            clientRepository.findById(it)
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

    @Transactional
    fun changeClientStatus(
        clientStage: ClientStage,
        client: Client,
        nextStage: ClientStage,
        activityDescription: String
    ): Client? {
        client.clientStage = clientStage
        client.nextStage = nextStage
        addClientActivity(client, activityDescription)
        return clientRepository.save(client);
    }

    fun addClientActivity(client: Client, description: String) {
        val authentication = SecurityContextHolder.getContext().authentication
        val activity = Activity(
            clientId = client.id,
            description = description,
            timestamp = LocalDateTime.now(),
            user = authentication.name
        )
        activityRepository.save(activity)

    }

    fun getClientActivities(clientId: Long): List<Activity> {
        return activityRepository.findByClientIdOrderByTimestampDesc(clientId)

    }

    fun getClientByEmail(email: String): Client? {
        val client = clientRepository.findByEmail(email) ?: throw NullPointerException("Client not found")
        return client
    }
}
