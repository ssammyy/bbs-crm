package com.bbs.bbsapi.services

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.ReportStatus
import com.bbs.bbsapi.models.SiteReport
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.AuditLog
import com.bbs.bbsapi.repos.ClientRepo
import com.bbs.bbsapi.repos.SiteReportRepository
import com.bbs.bbsapi.repos.AuditLogRepository
import com.bbs.bbsapi.security.CustomUserDetails
import org.springframework.stereotype.Service
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

@Service
class SiteReportService(
    private val siteReportRepository: SiteReportRepository,
    private val clientRepository: ClientRepo,
    private val clientService: ClientService,
    private val userService: UserService,
    private val digitalOceanService: DigitalOceanService,
    private val auditLogRepository: AuditLogRepository
) {
    fun findByClientId(clientId: Long): Map<String, Any?>? {
        val report = siteReportRepository.findByClientId(clientId) ?: return null
        val client = clientRepository.findById(clientId).orElseThrow {
            IllegalArgumentException("Client with ID $clientId not found")
        }
        // Fetch associated documents using DigitalOceanService
        val filesResponse: ResponseEntity<List<Map<String, Any?>>> = digitalOceanService.getClientFiles(client)
        val files = filesResponse.body ?: emptyList()

        return mapOf(
            "id" to report.id,
            "location" to report.location,
            "soilType" to report.soilType,
            "siteMeasurements" to report.siteMeasurements,
            "topography" to report.topography,
            "infrastructure" to report.infrastructure,
            "notes" to report.notes,
            "status" to report.status.toString(),
            "rejectionComments" to report.rejectionComments,
            "documents" to files
        )
    }

    @Transactional
    fun saveReport(report: SiteReport, clientId: Long, principal: Principal?): SiteReport {
        println("iko hapa 1")
        val client = clientRepository.findById(clientId).orElseThrow {
            IllegalArgumentException("Client with ID $clientId not found")
        }

        val existingReport = siteReportRepository.findByClientId(clientId)
        val username = principal?.name ?: userService.getLoggedInUser().username

        val newReport = report.copy(client = client)
        clientService.changeClientStatus(
            ClientStage.REQUIREMENTS_PENDING_DIRECTOR_APPROVAL,
            client,
            ClientStage.PROFORMA_INVOICE_GENERATION,
            "$username uploaded site report"
        )
        println("iko hapa 4")
        return siteReportRepository.save(newReport)
    }

    @Transactional
    fun approveReport(reportId: Long): SiteReport {
        val report = siteReportRepository.findById(reportId).orElseThrow {
            IllegalArgumentException("Report with ID $reportId not found")
        }
        val client = report.client ?: throw IllegalStateException("Client not associated with report")
        val username = userService.getLoggedInUser().username

        val updatedReport = report.copy(status = ReportStatus.APPROVED)
        clientService.changeClientStatus(
            ClientStage.PRELIMINARIES,
            client,
            ClientStage.LEGAL_APPROVALS,
            "$username approved site report"
        )
        return siteReportRepository.save(updatedReport)
    }

    @Transactional
    fun rejectReport(reportId: Long, comments: String): SiteReport {
        val report = siteReportRepository.findById(reportId).orElseThrow {
            IllegalArgumentException("Report with ID $reportId not found")
        }
        val client = report.client ?: throw IllegalStateException("Client not associated with report")
        val username = userService.getLoggedInUser().username

        val updatedReport = report.copy(
            status = ReportStatus.REJECTED,
            rejectionComments = comments
        )
        clientService.changeClientStatus(
            ClientStage.PENDING_SITE_VISIT,
            client,
            ClientStage.REQUIREMENTS_PENDING_DIRECTOR_APPROVAL,
            "$username rejected site report: $comments"
        )
        return siteReportRepository.save(updatedReport)
    }

    fun getAuditLogs(reportId: Long): List<AuditLog> {
        return auditLogRepository.findByReportId(reportId)
    }
}