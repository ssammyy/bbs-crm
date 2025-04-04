package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.LocationType
import com.bbs.bbsapi.enums.PreferredContactMethod
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import java.time.LocalDate

@Entity
@Table(name = "clients")
data class Client(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = true)
    var firstName: String,
    @Column(nullable = true)
    var secondName: String,
    @Column(nullable = true)
    var lastName: String,

    @Column(nullable = true, unique = true)
    var email: String?, // Email is optional
    @Column(nullable = false)
    var phoneNumber: String,
    @Column(nullable = false)
    var dob: LocalDate,
    @Column(nullable = false)
    var gender: String,
    @Enumerated(EnumType.STRING)
    var preferredContact: PreferredContactMethod,
    @Enumerated(EnumType.STRING)
    var location: LocationType,

    var country: String,
    var county: String?,
    var countryCode: String,
    @Column(nullable = false, unique = true)
    var idNumber: Long,

    @Enumerated(EnumType.STRING)
    var clientStage: ClientStage,
): BaseEntity(){
    constructor() : this(
        0, "", "", "", null, "", LocalDate.now(), "", PreferredContactMethod.EMAIL, LocationType.KENYA, "", null, "", 0, ClientStage.REGISTRATION,
    )
    fun ensureCountryConsistency() {
        if (location == LocationType.KENYA) {
            country = "Kenya"
        }
    }
}

