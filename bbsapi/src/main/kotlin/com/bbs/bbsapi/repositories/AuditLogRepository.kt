package com.bbs.bbsapi.repositories

import com.bbs.bbsapi.models.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, Long> {
    fun findByReportId(reportId: Long): List<AuditLog>
}