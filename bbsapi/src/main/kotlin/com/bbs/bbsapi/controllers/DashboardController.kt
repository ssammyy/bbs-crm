package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.services.DashboardDTO
import com.bbs.bbsapi.services.DashboardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/stats")
    fun getDashboardStats(): ResponseEntity<DashboardDTO> {
        try {
            val stats = dashboardService.getDashboardStats()
            return ResponseEntity.ok(stats)
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(null)
        }
    }

}
//TODO



