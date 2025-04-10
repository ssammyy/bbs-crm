package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface ClientRepo : JpaRepository<Client, Long> {
    fun findByPhoneNumber(phone: String): Client?
    fun findByIdNumber(idNumber: Long): Client?
    fun findByEmail(email: String): Client?
}
