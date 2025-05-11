package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.ClientDTO
import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.ContactStatus
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Activity
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.repos.*
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
    private val activityRepository: ActivityRepository,
    private val invoiceRepository: InvoiceRepository
) {
    @Transactional
    fun registerClient(clientDTO: ClientDTO): Client {
        val clientRole = roleRepository.findByName("CLIENT")
            .orElseThrow { RuntimeException("CLIENT role not found") }
        val user = User(
            username = clientDTO.email ?: clientDTO.phoneNumber,
            password = passwordEncoder.encode("defaultPassword123"),
            email = clientDTO.email.toString(),
            phonenumber = clientDTO.phoneNumber,
            role = clientRole
        )
        userRepository.save(user)

        val agent = clientDTO.agentId?.let {
            userRepository.findById(it)
                .orElseThrow { IllegalArgumentException("Agent with ID $it not found") }
        }

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
            clientStage = ClientStage.GENERATE_SITE_VISIT_INVOICE,
            nextStage = ClientStage.PENDING_DIRECTOR_SITE_VISIT_INVOICE_APPROVAL,
            clientSource = clientDTO.clientSource,
            projectName = clientDTO.projectName,
            projectActive = false,
            productOffering = clientDTO.productOffering!!,
            productTag = clientDTO.productTag!!,
            bankName = clientDTO.bankName,
            bankBranch = clientDTO.bankBranch,
            notes = clientDTO.notes,
            followUpDate = clientDTO.followUpDate,
            agent = agent
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
        val loggedInUser = userService.getLoggedInUser().username
        if (clientDTO.contactStatus == ContactStatus.ONBOARDED) {
            addClientActivity(client!!, "$loggedInUser changed on-boarded client ${client?.firstName} ${client?.lastName}  ")
        }
        addClientActivity(client!!, "$loggedInUser changed lead ${client?.firstName} ${client?.lastName}  status to ${client.contactStatus}  ")

        client?.country = clientDTO.country.toString()
        client?.email = clientDTO.email
        client?.firstName = clientDTO.firstName
        client?.secondName = clientDTO.secondName
        client?.lastName = clientDTO.surName
        client?.county = clientDTO.county
        client?.dob = clientDTO.dob
        client?.gender = clientDTO.gender
        client?.idNumber = clientDTO.idNumber
        client?.location = clientDTO.locationType
        client?.followUpDate = clientDTO.followUpDate
        client?.contactStatus = clientDTO.contactStatus!!
        client?.agent = clientDTO.agentId?.let {
            userRepository.findById(it)
                .orElseThrow { IllegalArgumentException("Agent with ID $it not found") }
        }
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
        if (clientStage == ClientStage.PENDING_SITE_VISIT) {
            val invoice = invoiceRepository.findByClientIdAndInvoiceType(client.id, InvoiceType.SITE_VISIT)
            invoice?.cleared = true
            invoiceRepository.save(invoice!!)
        }
        if (clientStage == ClientStage.ARCHITECTURAL_DRAWINGS_SKETCH) {
            val invoice = invoiceRepository.findByClientIdAndInvoiceType(client.id, InvoiceType.ARCHITECTURAL_DRAWINGS)
            invoice?.cleared = true
            invoiceRepository.save(invoice!!)
        }
        if (clientStage == ClientStage.UPLOAD_BOQ) {
            val invoice = invoiceRepository.findByClientIdAndInvoiceType(client.id, InvoiceType.BOQ)
            invoice?.cleared = true
            invoiceRepository.save(invoice!!)
        }

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

    fun getLeads(): List<Client> {
        return clientRepository.findByContactStatusNot(ContactStatus.ONBOARDED)
    }

    fun getOnBoardedClients(): List<Client> {
        return clientRepository.findByContactStatus(ContactStatus.ONBOARDED)
    }
}