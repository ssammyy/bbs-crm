package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.SiteReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SiteReportRepository : JpaRepository<SiteReport, Long>{
    fun findByClientId(clientId: Long): SiteReport?
}