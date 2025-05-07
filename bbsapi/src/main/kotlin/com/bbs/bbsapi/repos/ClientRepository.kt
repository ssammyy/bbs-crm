package com.bbs.bbsapi.repos

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.ContactStatus
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.ClientCommitment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


@Repository
interface ClientRepo : JpaRepository<Client, Long> {
    fun findByPhoneNumber(phone: String): Client?
    fun findByIdNumber(idNumber: Long): Client?
    fun findByEmail(email: String): Client?
    fun countByClientStage(stage: ClientStage): Long
    @Query("SELECT c.clientSource, COUNT(c) FROM Client c GROUP BY c.clientSource")
    fun countByClientSource(): List<Array<Any>>
    fun findByContactStatus(status: ContactStatus): List<Client>
    fun findByContactStatusNot(contactStatus: ContactStatus): MutableList<Client>

}

@Repository
interface ClientCommitmentRepository : JpaRepository<ClientCommitment, Long> {
    fun findByFollowUpDateBeforeAndContactStatusNot(date: LocalDateTime, status: ContactStatus): List<ClientCommitment>
    fun findByContactStatusNot(status: ContactStatus): List<ClientCommitment>
    fun findByContactStatus(status: ContactStatus): List<ClientCommitment>
}
