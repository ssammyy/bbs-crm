package com.bbs.bbsapi.models

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "client_contracts")
data class ClientContract(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val clientId: Long? = null,
    val projectName: String? = null,
    val boqAmount: Double? = null,
    val projectStartDate: LocalDate? = null,
    val projectDuration: Int? = null,
    val contractFileUrl: String? = null,
    val status: String = "ACTIVE",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_invoice_id")
    val mainInvoice: Invoice? = null,
    val moneyUsedSoFar: Double? = null,
    val lastReportFileUrl: String? = null
)