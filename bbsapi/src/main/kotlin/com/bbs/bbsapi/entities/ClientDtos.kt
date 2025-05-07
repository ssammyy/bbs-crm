package com.bbs.bbsapi.entities

import com.bbs.bbsapi.enums.*
import java.time.LocalDate
import java.time.LocalDateTime


data class ClientDTO(
    val id:Long? = null,
    val firstName: String,
    val secondName: String,
    val surName: String,
    val email: String?,
    val gender: String,
    val phoneNumber: String,
    val dob: LocalDate,
    val preferredContact: PreferredContactMethod,
    val locationType: LocationType,
    val county: String? =null,
    val country: String? = null,
    val countryCode: String,
    val idNumber: Long,
    val clientSource: String,
    val projectName: String,
    val invoiceNumber: PdfInvoiceDTO? = null,
    val productOffering: ProductOffering? = null,
    val productTag: ProductTag? = null,
    val bankName: String? = null,
    val bankBranch: String? = null,
    val consultancySubtag: ConsultancySubtag? = null,
    val notes: String? = null,
    val followUpDate: LocalDateTime,
    val contactStatus: ContactStatus? = null,
    val agentId: Long? = null




    )

data class LocationDTO(
    val isKenyan: Boolean,
    val county: String?,
    val country: String
)

data class UpdateStageRequest(
    val currentStage: String,
    val newStage: String,
    val message: String
)

data class ClientCommitmentDTO(
    val name: String,
    val email: String,
    val phone: String,
    val notes: String? = null,
    val followUpDate: LocalDateTime
)
