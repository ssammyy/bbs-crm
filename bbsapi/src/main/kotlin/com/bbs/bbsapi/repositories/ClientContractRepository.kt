package com.bbs.bbsapi.repositories

import com.bbs.bbsapi.models.ClientContract
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientContractRepository : JpaRepository<ClientContract, Long> {
    fun findByClientId(clientId: Long): ClientContract?
} 