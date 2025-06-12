package com.bbs.bbsapi.repositories

import com.bbs.bbsapi.models.Activity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ActivityRepository : JpaRepository<Activity, Long> {
    fun findByClientIdOrderByTimestampDesc(clientId: Long): List<Activity>
    fun findTop10ByOrderByTimestampDesc(): List<Activity>
    fun findTop10ByClientIdOrderByTimestampDesc(clientId: Long): List<Activity>
    fun findByClientIdIn(clientIds: List<Long>): List<Activity>

}