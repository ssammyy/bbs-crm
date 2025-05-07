package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.models.SiteReport
import com.bbs.bbsapi.services.SiteReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import kotlin.math.log

@RestController
@RequestMapping("/api/site-report")
class SiteReportController(private val service: SiteReportService) {
    @GetMapping("/client/{clientId}")
    fun getReportByClientId(@PathVariable clientId: Long): ResponseEntity<Map<String, Any?>> {
        val report = service.findByClientId(clientId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(report)
    }

    @PostMapping
    fun submitReport(
        @RequestBody request: Map<String, Any>,
        principal: Principal?
    ): ResponseEntity<SiteReport> {
        println("Updating report >>>")
        val clientId = (request["clientId"] as? Number)?.toLong() ?: throw IllegalArgumentException("clientId is required")
        val reportId = (request["id"] as? Number)?.toLong()
        val infrastructure = (request["infrastructure"] as? List<*>)?.map { it.toString() } ?: emptyList()
        val report = SiteReport(
            id = reportId,
            location = request["location"] as String,
            soilType = request["soilType"] as String,
            siteMeasurements = request["siteMeasurements"] as String,
            topography = request["topography"] as String,
            infrastructure = infrastructure,
            notes = request["notes"] as? String
        )
        val savedReport = service.saveReport(report, clientId, principal)
        return ResponseEntity.ok(savedReport)
    }

    @PostMapping("/{reportId}/approve")
    fun approveReport(@PathVariable reportId: Long): ResponseEntity<SiteReport> {
        val updatedReport = service.approveReport(reportId)
        return ResponseEntity.ok(updatedReport)
    }

    @PostMapping("/{reportId}/reject")
    fun rejectReport(@PathVariable reportId: Long, @RequestBody request: Map<String, String>): ResponseEntity<SiteReport> {
        val comments = request["comments"] ?: throw IllegalArgumentException("Comments are required for rejection")
        val updatedReport = service.rejectReport(reportId, comments)
        return ResponseEntity.ok(updatedReport)
    }

//    @GetMapping("/{reportId}/audit-logs")
//    fun getAuditLogs(@PathVariable reportId: Long): ResponseEntity<List<Map<String, Any>>> {
//        val auditLogs = service.getAuditLogs(reportId)
//        val response = auditLogs.map { log ->
//            mapOf(
//                "id" to log.id,
//                "reportId" to log.reportId,
//                "changedBy" to log.changedBy,
//                "fieldName" to log.fieldName,
//                "oldValue" to log.oldValue,
//                "newValue" to log.newValue,
//                "changeDate" to log.changeDate.toString()
//            )
//        }
//        return ResponseEntity.ok(response)
//    }
}