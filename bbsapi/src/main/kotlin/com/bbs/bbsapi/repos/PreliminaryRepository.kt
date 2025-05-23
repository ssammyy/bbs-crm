package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.Preliminary
import com.bbs.bbsapi.models.PreliminaryType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PreliminaryRepository : JpaRepository<Preliminary, Long> {
    fun findByClientId(clientId: Long): List<Preliminary>
    fun findByInvoiceId(invoiceId: Long): List<Preliminary>
    fun existsByPreliminaryType_Id(preliminaryTypeId: Long): Boolean
    fun findByClientIdAndPreliminaryType_Id(clientId: Long, preliminaryTypeId: Long): Preliminary?
}
@Repository
interface PreliminaryTypeRepository : JpaRepository<PreliminaryType, Long> {
    abstract fun findByName(name: String): Optional<PreliminaryType>
}