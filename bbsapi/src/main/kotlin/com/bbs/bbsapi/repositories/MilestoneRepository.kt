package com.bbs.bbsapi.repositories

import com.bbs.bbsapi.models.Milestone
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MilestoneRepository : JpaRepository<Milestone, Long> {
    fun findByClientIdOrderByOrderAsc(clientId: Long): List<Milestone>
    fun existsByClientIdAndName(clientId: Long, name: String): Boolean
} 