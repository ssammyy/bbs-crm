package com.bbs.bbsapi.dtos

import com.bbs.bbsapi.enums.*
import java.time.LocalDate
import java.time.LocalDateTime

data class ClientDetailsDTO(
    val id: Long,
    val firstName: String,
    val secondName: String,
    val lastName: String,
    val email: String?,
    val phoneNumber: String,
    val dob: LocalDate,
    val gender: String,
    val preferredContact: PreferredContactMethod,
    val location: LocationType,
    val country: String,
    val county: String?,
    val countryCode: String,
    val idNumber: Long?,
    val clientStage: ClientStage,
    val nextStage: ClientStage,
    val clientSource: String,
    val projectName: String,
    val projectActive: Boolean,
    val productOffering: ProductOffering,
    val productTag: ProductTag,
    val bankName: String?,
    val bankBranch: String?,
    val consultancySubtags: List<ConsultancySubtag>,
    val notes: String?,
    val followUpDate: LocalDateTime,
    val contactStatus: ContactStatus,
    val siteVisitDone: Boolean,
    val siteReport: SiteReportDTO?
)

data class SiteReportDTO(
    val id: Long?,
    val location: String,
    val soilType: String,
    val siteMeasurements: String,
    val topography: String,
    val infrastructure: List<String>,
    val notes: String?,
    val status: ReportStatus,
    val rejectionComments: String?
) 