package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.services.AgentDTO
import com.bbs.bbsapi.services.DashboardDTO
import com.bbs.bbsapi.services.DashboardService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {
    private val log = LoggerFactory.getLogger(DashboardService::class.java)

    @GetMapping("/stats")
    fun getDashboardStats(): ResponseEntity<DashboardDTO> {
        try {
            val stats = dashboardService.getDashboardStats()
            return ResponseEntity.ok(stats)
        } catch (e: Exception) {
            log.error("Error during getting dashboard stats", e)
            return ResponseEntity.status(500).body(null)
        }
    }
    @GetMapping("/agent-stats")
    fun getAgentStats(): ResponseEntity<AgentDTO> {
        try {
            val stats = dashboardService.getAgentDashboardStats()
            return ResponseEntity.ok(stats)
        } catch (e: Exception) {
            log.error("Error during getting dashboard stats", e)
            return ResponseEntity.status(500).body(null)
        }
    }

}
//TODO



