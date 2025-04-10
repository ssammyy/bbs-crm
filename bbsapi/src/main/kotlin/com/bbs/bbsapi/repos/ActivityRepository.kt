package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.Activity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ActivityRepository : JpaRepository<Activity, Long> {
    fun findByClientIdOrderByTimestampDesc(clientId: Long): List<Activity>
}