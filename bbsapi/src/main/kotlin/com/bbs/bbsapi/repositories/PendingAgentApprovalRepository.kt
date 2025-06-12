package com.bbs.bbsapi.repositories

import com.bbs.bbsapi.models.PendingAgentApproval
import com.bbs.bbsapi.models.ApprovalStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PendingAgentApprovalRepository : JpaRepository<PendingAgentApproval, Long> {
    fun findByStatus(status: ApprovalStatus): List<PendingAgentApproval>
    fun findByUsername(username: String): PendingAgentApproval?
    fun findByEmail(email: String): PendingAgentApproval?
} 