package com.bbs.bbsapi.entities

import com.bbs.bbsapi.enums.LocationType
import com.bbs.bbsapi.enums.PreferredContactMethod
import java.time.LocalDate


data class ClientDTO(
    val firstName: String,
    val secondName: String,
    val surName: String,
    val email: String?,
    val gender: String,
    val phoneNumber: String,
    val dob: LocalDate,
    val preferredContact: PreferredContactMethod,
    val locationType: LocationType,
    val county: String,
    val country: String,
    val countryCode: String,
    val idNumber: Long

)

data class LocationDTO(
    val isKenyan: Boolean,
    val county: String?,
    val country: String
)
