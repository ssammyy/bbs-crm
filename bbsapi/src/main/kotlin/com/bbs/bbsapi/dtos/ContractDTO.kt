package com.bbs.bbsapi.dtos

import java.time.LocalDate

// DTO for contract creation from frontend

data class ContractDTO(
    val clientId: Long,
    val projectName: String,
    val boqAmount: Double,
    val projectStartDate: LocalDate,
    val projectDuration: Int,
    val installments: List<InstallmentDTO>,
    val contractFileUrl: String? = null
)

data class InstallmentDTO(
    val description: String,
    val percentage: Double,
    val amount: Double,
    val dueDate: LocalDate
) 