package com.bbs.bbsapi.services

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.ContactStatus
import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.enums.InvoiceType
import com.bbs.bbsapi.repos.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DashboardService(
    private val clientRepository: ClientRepo,
    private val invoiceRepository: InvoiceRepository,
    private val activityRepository: ActivityRepository,
    private val fileMetadataRepository: FileRepository,
    private val clientCommitmentRepository: ClientCommitmentRepository
) {

    // Predefined list of client sources
    private val clientSources = listOf(
        "socialMedia",
        "walkins",
        "activations",
        "website"
    )

    fun getDashboardStats(): DashboardDTO {
        // Client Stage Distribution
        val clientStageDistribution = ClientStage.values().associateWith { stage ->
            clientRepository.countByClientStage(stage)
        }

        // Client Source Distribution
        val rawSourceCounts = clientRepository.countByClientSource()
        val sourceCountsMap = rawSourceCounts.associate { result ->
            val source = result[0] as String
            val count = (result[1] as Long)
            source to count
        }
        // Ensure all predefined sources are included, even if their count is 0
        val clientSourceDistribution = clientSources.associateWith { source ->
            sourceCountsMap[source] ?: 0L
        }

        // Invoice Stats
        val totalRevenue = invoiceRepository.findByClearedTrue().sumOf { it.total }
        val pendingToBeClearedAmount = invoiceRepository.findByClearedFalseAndInvoiceTypeNot(InvoiceType.PROFORMA).sumOf { it.total }

        // Cleared vs. Uncleared Breakdown
        val clearedInvoicesCount = invoiceRepository.countByClearedTrue()
        val unclearedInvoicesCount = invoiceRepository.countByClearedFalse()
        val clearedVsUnclearedBreakdown = mapOf(
            "Cleared" to clearedInvoicesCount,
            "Uncleared" to unclearedInvoicesCount
        )
        val totalCommitments = clientCommitmentRepository.findByContactStatusNot(ContactStatus.ONBOARDED).count()

        // Highest Revenue by Client (based on cleared invoices)
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

        // Recent Activities
        val recentActivities = activityRepository.findTop10ByOrderByTimestampDesc().map {
            ActivityDTO(
                id = it.id,
                clientId = it.clientId,
                description = it.description,
                timestamp = it.timestamp,
                user = it.user
            )
        }

        // File Upload Stats
        val totalFiles = fileMetadataRepository.count()
        val fileTypeBreakdown = FileType.values().associateWith { type ->
            fileMetadataRepository.countByFileType(type)
        }

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
            commitMentClients = totalCommitments
        )
    }
}

data class DashboardDTO(
    val clientStageDistribution: Map<ClientStage, Long>,
    val clientSourceDistribution: Map<String, Long>, // Map of string to count
    val invoiceStats: InvoiceStatsDTO,
    val recentActivities: List<ActivityDTO>,
    val clearedVsUnclearedBreakdown: Map<String, Long>,
    val highestRevenueClient: HighestRevenueClientDTO,
    val fileStats: FileStatsDTO,
    val commitMentClients: Int
)

data class InvoiceStatsDTO(
    val totalRevenue: Double,
    val pendingToBeClearedAmount: Double
)

data class ActivityDTO(
    val id: Long,
    val clientId: Long,
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
    val fileTypeBreakdown: Map<FileType, Long>
)

data class commitmentClientDTO(
    val totalCommitmentClients: Long,

)