package com.bbs.bbsapi.repositories

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.ContactStatus
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.ClientCommitment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


@Repository
interface ClientRepository : JpaRepository<Client, Long> {
    fun findByPhoneNumberAndSoftDeleteFalse(phone: String): Client?
    fun findByIdNumberAndSoftDeleteFalse(idNumber: Long): Client?
    fun findByEmailAndSoftDeleteFalse(email: String): Client?
    fun countByClientStageAndSoftDeleteFalse(stage: ClientStage): Long
    @Query("SELECT c.clientSource, COUNT(c) FROM Client c WHERE c.softDelete = false GROUP BY c.clientSource")
    fun countByClientSource(): List<Array<Any>>
    fun findByContactStatusAndSoftDeleteFalse(status: ContactStatus): List<Client>
    fun findByContactStatusNotAndSoftDeleteFalse(contactStatus: ContactStatus): MutableList<Client>
    fun findByAgentIdAndSoftDeleteFalse(agentId: Long): List<Client>
    fun countByCreatedOnBetweenAndSoftDeleteFalse(start: LocalDateTime?, end: LocalDateTime?): Long
    fun findAllBySoftDeleteFalse(): List<Client>
    fun findByContactStatusNot(onboarded: ContactStatus): List<Client>
}

@Repository
interface ClientCommitmentRepository : JpaRepository<ClientCommitment, Long> {
    fun findByFollowUpDateBeforeAndContactStatusNot(date: LocalDateTime, status: ContactStatus): List<ClientCommitment>
    fun findByContactStatusNot(status: ContactStatus): List<ClientCommitment>
    fun findByContactStatus(status: ContactStatus): List<ClientCommitment>
}
