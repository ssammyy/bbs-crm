package com.bbs.bbsapi.services

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.ContactStatus
import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.YearMonth

@Service
class DashboardService(
    private val clientRepository: ClientRepository,
    private val invoiceRepository: InvoiceRepository,
    private val activityRepository: ActivityRepository,
    private val fileMetadataRepository: FileRepository,
    private val clientCommitmentRepository: ClientCommitmentRepository,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val clientRepo: ClientRepository,
    private val clientService: ClientService,
    private val roleRepository: RoleRepository,
    private val clientContractRepository: ClientContractRepository
) {
    private val log = LoggerFactory.getLogger(DashboardService::class.java)

    // Predefined list of client sources
    private val clientSources = listOf(
        "socialMedia",
        "walkins",
        "activations",
        "website"
    )

    fun getAgentDashboardStats(): AgentDTO {
        val authentication = SecurityContextHolder.getContext().authentication
        val agentUserName = authentication.name
        val agentId = agentUserName?.let { userRepository.findByUsername(it)?.id }
        log.info("getting dashboard stats for agentId=$agentId")
        return agentId?.let { getAgentDashboardStats(it) }!!
    }

    fun getDashboardStats(): DashboardDTO {
        val authentication = SecurityContextHolder.getContext().authentication
        val isClient = authentication.authorities.any { it.authority == "VIEW_CLIENT_PROFILE" }

        val clientUserName = if (isClient) authentication.name else null
        val clientId = clientUserName?.let { clientRepository.findByEmailAndSoftDeleteFalse(it)?.id }

        if (isClient && clientId != null) {
            log.info("getting dashboard stats for clientId=$clientId")
            return getClientDashboardStats(clientId)
        }

        // Admin Dashboard Logic
        val clientStageDistribution = ClientStage.values().associateWith { stage ->
            clientRepository.countByClientStageAndSoftDeleteFalse(stage)
        }.mapKeys { it.key.name }

        val rawSourceCounts = clientRepository.countByClientSource()
        val sourceCountsMap = rawSourceCounts.associate { result ->
            val source = result[0] as String
            val count = (result[1] as Long)
            source to count
        }
        val clientSourceDistribution = clientSources.associateWith { source ->
            sourceCountsMap[source] ?: 0L
        }

        val totalRevenue = invoiceRepository.findByClearedTrue().sumOf { it.total }
        val pendingToBeClearedAmount =
            invoiceRepository.findByClearedFalseAndInvoiceTypeNot(InvoiceType.PROFORMA).sumOf { it.total }

        val clearedInvoicesCount = invoiceRepository.countByClearedTrue()
        val unclearedInvoicesCount = invoiceRepository.countByClearedFalse()
        val clearedVsUnclearedBreakdown = mapOf(
            "Cleared" to clearedInvoicesCount,
            "Uncleared" to unclearedInvoicesCount
        )
        val totalCommitments = clientCommitmentRepository.findByContactStatusNot(ContactStatus.ONBOARDED).count()

        val revenueByClient = invoiceRepository.findByClearedTrue()
            .groupBy { it.clientId }
            .mapValues { entry -> entry.value.sumOf { it.total } }
        val highestRevenueClient = revenueByClient.maxByOrNull { it.value }
        val highestRevenueClientDetails = if (highestRevenueClient != null) {
            val client = clientRepository.findById(highestRevenueClient.key).orElse(null)
            HighestRevenueClientDTO(
                clientId = highestRevenueClient.key,
                clientName = client?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
                totalRevenue = highestRevenueClient.value
            )
        } else {
            HighestRevenueClientDTO(clientId = 0, clientName = "N/A", totalRevenue = 0.0)
        }

        val recentActivities = activityRepository.findTop10ByOrderByTimestampDesc().map {
            ActivityDTO(
                id = it.id,
                clientId = it.clientId,
                client = clientRepository.findById(it.clientId).orElse(null),
                description = it.description,
                timestamp = it.timestamp,
                user = it.user
            )
        }

        val totalFiles = fileMetadataRepository.count()
        val fileTypeBreakdown = FileType.values().associateWith { type ->
            fileMetadataRepository.countByFileType(type)
        }.mapKeys { it.key.name }

        // New metrics
        val totalAgents = userRepository.countUserByRole_Name("AGENT")
        val totalSystemUsers = userRepository.countUserByRoleNot(roleRepository.findByName("CLIENT").get())
        val superAdmins = userRepository.findByRole_Name("SUPER_ADMIN").map {
            SuperAdminDTO(
                id = it.id!!,
                username = it.username,
                phone = it.phonenumber
            )
        }

        // Revenue over time (last 12 months)
        val now = LocalDateTime.now()
        val revenueOverTime = (0..11).map { i ->
            val yearMonth = YearMonth.now().minusMonths(i.toLong())
            val start = yearMonth.atDay(1).atStartOfDay()
            val end = yearMonth.atEndOfMonth().atTime(23, 59, 59)
            val revenue = invoiceRepository.findByClearedTrueAndDateIssuedBetween(start.toLocalDate(), end.toLocalDate()).sumOf { it.total }
            TimeSeriesDataDTO(
                period = yearMonth.toString(),
                value = revenue
            )
        }.reversed()

        // Clients over time (last 12 months)
        val clientsOverTime = (0..11).map { i ->
            val yearMonth = YearMonth.now().minusMonths(i.toLong())
            val start = yearMonth.atDay(1).atStartOfDay()
            val end = yearMonth.atEndOfMonth().atTime(23, 59, 59)
            val count = clientRepository.countByCreatedOnBetweenAndSoftDeleteFalse(start, end)
            TimeSeriesDataDTO(
                period = yearMonth.toString(),
                value = count.toDouble()
            )
        }.reversed()

        return DashboardDTO(
            clientStageDistribution = clientStageDistribution,
            clientSourceDistribution = clientSourceDistribution,
            invoiceStats = InvoiceStatsDTO(
                totalRevenue = totalRevenue,
                pendingToBeClearedAmount = pendingToBeClearedAmount
            ),
            recentActivities = recentActivities,
            clearedVsUnclearedBreakdown = clearedVsUnclearedBreakdown,
            highestRevenueClient = highestRevenueClientDetails,
            fileStats = FileStatsDTO(
                totalFiles = totalFiles,
                fileTypeBreakdown = fileTypeBreakdown
            ),
            commitMentClients = totalCommitments,
            totalAgents = totalAgents,
            totalSystemUsers = totalSystemUsers,
            superAdmins = superAdmins,
            revenueOverTime = revenueOverTime,
            clientsOverTime = clientsOverTime,
            totalActiveClients = clientService.getOnBoardedClients().count().toLong()
        )
    }

    private fun getAgentDashboardStats(agentId: Long): AgentDTO {
        val clients = clientRepository.findByAgentIdAndSoftDeleteFalse(agentId)
        val noOfClients = clients.count()
        val clientIdList = clients.map { it.id }
        val clientMap = clients.associateBy { it.id }
        val user = userRepository.findById(agentId).get()
        val commissionPercentage = user.commissionPercentage
        // Fetch all contracts for these clients
        val contracts = clientContractRepository.findByClientIdIn(clientIdList)
        val contractDTOs = contracts.map { contract ->
            val client = contract.clientId?.let { clientMap[it] }
            val clientName = listOfNotNull(client?.firstName, client?.lastName).joinToString(" ")
            val potentialRevenue = (commissionPercentage ?: 0L) / 100.0 * (contract.boqAmount ?: 0.0)
            val revenueEarned = (commissionPercentage ?: 0L) / 100.0 * (contract.moneyUsedSoFar ?: 0.0)
            val installmentInvoices = invoiceRepository.findByClientId(contract.clientId ?: 0L)
                .filter { it.invoiceType.name.contains("INSTALLMENT") }
                .map { InvoiceSummaryDTO(
                    invoiceId = it.id,
                    amount = it.total,
                    cleared = it.cleared,
                    dateIssued = it.dateIssued?.toString()
                ) }
            AgentContractDTO(
                contractId = contract.id,
                clientId = contract.clientId,
                clientName = clientName,
                projectName = contract.projectName,
                boqAmount = contract.boqAmount,
                moneyUsedSoFar = contract.moneyUsedSoFar,
                commissionPercentage = commissionPercentage,
                potentialRevenue = potentialRevenue,
                revenueEarned = revenueEarned,
                status = contract.status,
                installmentInvoices = installmentInvoices
            )
        }
        // Calculate total potential revenue (sum of all contract commissions)
        val totalPotentialRevenue = contractDTOs.sumOf { it.potentialRevenue ?: 0.0 }
        // Calculate pending installment revenue (sum of commission% × amount for all unpaid installments)
        val pendingInstallmentRevenue = contractDTOs.sumOf { contract ->
            contract.installmentInvoices.filter { !it.cleared }.sumOf { (contract.commissionPercentage ?: 0L) / 100.0 * (it.amount) }
        }
        // Commission-based revenue over time
        val now = LocalDateTime.now()
        val revenueOverTime = (0..11).map { i ->
            val yearMonth = YearMonth.now().minusMonths(i.toLong())
            val start = yearMonth.atDay(1).atStartOfDay()
            val end = yearMonth.atEndOfMonth().atTime(23, 59, 59)
            val monthContracts = contracts.filter { contract ->
                contract.updatedAt.isAfter(start) && contract.updatedAt.isBefore(end)
            }
            val monthRevenue = monthContracts.sumOf { (commissionPercentage ?: 0L) / 100.0 * (it.moneyUsedSoFar ?: 0.0) }
            TimeSeriesDataDTO(
                period = yearMonth.toString(),
                value = monthRevenue
            )
        }.reversed()
        val invoiceStats = InvoiceStatsDTO(
            totalRevenue = totalPotentialRevenue,
            pendingToBeClearedAmount = pendingInstallmentRevenue
        )
        val recentActivities = activityRepository.findByClientIdIn(clientIdList).map {
            ActivityDTO(
                id = it.id,
                clientId = it.clientId,
                client = clientRepository.findById(it.clientId).orElse(null),
                description = it.description,
                timestamp = it.timestamp,
                user = it.user
            )
        }
        return AgentDTO(
            numberOfClients = noOfClients,
            clients = clients,
            invoiceStats = invoiceStats,
            recentActivities = recentActivities,
            revenueOverTime = revenueOverTime,
            commissionPercentage = commissionPercentage ?: 0L,
            contracts = contractDTOs,
            totalPotentialRevenue = totalPotentialRevenue,
            pendingInstallmentRevenue = pendingInstallmentRevenue
        )
    }

    private fun getClientDashboardStats(clientId: Long): DashboardDTO {
        val client = clientRepository.findById(clientId).orElseThrow { IllegalArgumentException("Client not found") }

        try {
            val invoices = invoiceRepository.findByClientId(clientId)

            val totalRevenue = invoices.sumOf { it.total - it.balance }
            val pendingToBeClearedAmount = invoices.sumOf { it.balance }

            val clearedInvoicesCount = invoiceRepository.countByClientIdAndClearedTrue(clientId)
            val unclearedInvoicesCount = invoiceRepository.countByClientIdAndClearedFalse(clientId)
            val clearedVsUnclearedBreakdown = mapOf(
                "Cleared" to clearedInvoicesCount,
                "Uncleared" to unclearedInvoicesCount
            )

            val recentActivities = activityRepository.findTop10ByClientIdOrderByTimestampDesc(clientId).map {
                ActivityDTO(
                    id = it.id,
                    clientId = it.clientId,
                    client = clientRepository.findById(it.clientId).orElse(null),
                    description = it.description,
                    timestamp = it.timestamp,
                    user = it.user
                )
            }

            return DashboardDTO(
                clientStageDistribution = mapOf(client.clientStage.name to 1L),
                clientSourceDistribution = mapOf(client.clientSource to 1L),
                invoiceStats = InvoiceStatsDTO(
                    totalRevenue = totalRevenue,
                    pendingToBeClearedAmount = pendingToBeClearedAmount
                ),
                recentActivities = recentActivities,
                clearedVsUnclearedBreakdown = clearedVsUnclearedBreakdown,
                highestRevenueClient = HighestRevenueClientDTO(
                    clientId = clientId,
                    clientName = "${client.firstName} ${client.lastName}",
                    totalRevenue = totalRevenue
                ),
                fileStats = FileStatsDTO(
                    totalFiles = fileMetadataRepository.countByClientId(clientId),
                    fileTypeBreakdown = FileType.values().associateWith { type ->
                        fileMetadataRepository.countByClientIdAndFileType(clientId, type)
                    }.mapKeys { it.key.name }
                ),
                commitMentClients = if (client.contactStatus != ContactStatus.ONBOARDED) 1 else 0,
                projectDetails = ProjectDetailsDTO(
                    projectName = client.projectName,
                    clientStage = client.clientStage.toString(),
                    productOffering = client.productOffering.toString(),
                    projectActive = client.projectActive
                ),
                totalAgents = 0,
                totalSystemUsers = 0,
                superAdmins = emptyList(),
                revenueOverTime = emptyList(),
                clientsOverTime = emptyList(),
                totalActiveClients = 0
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}

data class DashboardDTO(
    val clientStageDistribution: Map<String, Long>,
    val clientSourceDistribution: Map<String, Long>,
    val invoiceStats: InvoiceStatsDTO,
    val recentActivities: List<ActivityDTO>,
    val clearedVsUnclearedBreakdown: Map<String, Long>,
    val highestRevenueClient: HighestRevenueClientDTO,
    val fileStats: FileStatsDTO,
    val commitMentClients: Int,
    val projectDetails: ProjectDetailsDTO? = null,
    val totalAgents: Long,
    val totalSystemUsers: Long,
    val superAdmins: List<SuperAdminDTO>,
    val revenueOverTime: List<TimeSeriesDataDTO>,
    val clientsOverTime: List<TimeSeriesDataDTO>,
    val totalActiveClients: Long,
)

data class AgentDTO(
    val numberOfClients: Int,
    val clients: List<Client>,
    val invoiceStats: InvoiceStatsDTO,
    val recentActivities: List<ActivityDTO>,
    val revenueOverTime: List<TimeSeriesDataDTO>,
    val commissionPercentage: Long,
    val contracts: List<AgentContractDTO>,
    val totalPotentialRevenue: Double,
    val pendingInstallmentRevenue: Double
)

data class InvoiceStatsDTO(
    val totalRevenue: Double,
    val pendingToBeClearedAmount: Double
)

data class ActivityDTO(
    val id: Long,
    val clientId: Long,
    val client: Client?,
    val description: String,
    val timestamp: LocalDateTime,
    val user: String?
)

data class HighestRevenueClientDTO(
    val clientId: Long,
    val clientName: String,
    val totalRevenue: Double
)

data class FileStatsDTO(
    val totalFiles: Long,
    val fileTypeBreakdown: Map<String, Long>
)

data class ProjectDetailsDTO(
    val projectName: String,
    val clientStage: String,
    val productOffering: String,
    val projectActive: Boolean
)

data class SuperAdminDTO(
    val id: Long,
    val username: String,
    val phone: String
)

data class TimeSeriesDataDTO(
    val period: String,
    val value: Double
)

data class AgentContractDTO(
    val contractId: Long,
    val clientId: Long?,
    val clientName: String?,
    val projectName: String?,
    val boqAmount: Double?,
    val moneyUsedSoFar: Double?,
    val commissionPercentage: Long?,
    val potentialRevenue: Double?,
    val revenueEarned: Double?,
    val status: String?,
    val installmentInvoices: List<InvoiceSummaryDTO> = emptyList()
)

data class InvoiceSummaryDTO(
    val invoiceId: Long,
    val amount: Double,
    val cleared: Boolean,
    val dateIssued: String?
)