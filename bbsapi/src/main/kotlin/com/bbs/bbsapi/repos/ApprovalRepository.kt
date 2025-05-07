package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.Approval
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ApprovalRepository : JpaRepository<Approval, Long> {
    fun findByPreliminaryId(preliminaryId: Long): List<Approval>
}