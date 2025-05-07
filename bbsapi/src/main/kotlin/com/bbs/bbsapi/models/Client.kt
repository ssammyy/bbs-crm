package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.*
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

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
    var email: String?,
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
    @Enumerated(EnumType.STRING)
    var nextStage: ClientStage,

    var clientSource: String,
    var projectName: String,
    var projectActive: Boolean,

    // Enum-based fields
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var productOffering: ProductOffering,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var productTag: ProductTag,

    // Fields for Jenga Kwako
    @Column(nullable = true)
    var bankName: String? = null,
    @Column(nullable = true)
    var bankBranch: String? = null,

    // Enum-based consultancy subtags
    @ElementCollection
    @CollectionTable(name = "client_consultancy_subtags", joinColumns = [JoinColumn(name = "client_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "subtag")
    var consultancySubtags: MutableList<ConsultancySubtag> = mutableListOf(),
    @Column
    var notes: String? = null,

    @Column(nullable = false)
    var followUpDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var contactStatus: ContactStatus = ContactStatus.LEAD,

    @OneToOne(mappedBy = "client")
    @JsonBackReference
    val siteReport: SiteReport? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "agent_id", referencedColumnName = "id")
    var agent: User? = null
) : BaseEntity() {
    constructor() : this(
        id = 0,
        firstName = "",
        secondName = "",
        lastName = "",
        email = null,
        phoneNumber = "",
        dob = LocalDate.now(),
        gender = "",
        preferredContact = PreferredContactMethod.EMAIL,
        location = LocationType.KENYA,
        country = "",
        county = null,
        countryCode = "",
        idNumber = 0,
        clientStage = ClientStage.GENERATE_SITE_VISIT_INVOICE,
        nextStage = ClientStage.PENDING_SITE_VISIT,
        clientSource = "",
        projectName = "",
        projectActive = false,
        productOffering = ProductOffering.JENGA_KWAKO,
        productTag = ProductTag.JENGA_KWAKO,
        notes = "",
        followUpDate = LocalDateTime.now()
    )

    fun ensureCountryConsistency() {
        if (location == LocationType.KENYA) {
            country = "Kenya"
        }
    }

    fun validateProductFields() {
        when (productOffering) {
            ProductOffering.JENGA_KWAKO -> {
                require(bankName != null) { "Bank Name is required for Jenga Kwako" }
                require(bankBranch != null) { "Bank Branch is required for Jenga Kwako" }
                require(productTag == ProductTag.JENGA_KWAKO) { "Invalid tag for Jenga Kwako" }
            }

            ProductOffering.JENGA_STRESS_FREE -> {
                require(
                    productTag in listOf(
                        ProductTag.JENGA_STRESS_FREE_COMMERCIAL,
                        ProductTag.JENGA_STRESS_FREE_RESIDENTIAL
                    )
                ) { "Invalid tag for Jenga Stress Free" }
            }

            ProductOffering.LABOUR_ONLY_CONTRACTING -> {
                require(
                    productTag in listOf(
                        ProductTag.LABOUR_ONLY_COMMERCIAL,
                        ProductTag.LABOUR_ONLY_RESIDENTIAL
                    )
                ) { "Invalid tag for Labour Only Contracting" }
            }

            ProductOffering.DIASPORA_BUILDING_SOLUTIONS -> {
                require(
                    productTag in listOf(
                        ProductTag.DIASPORA_COMMERCIAL,
                        ProductTag.DIASPORA_RESIDENTIAL
                    )
                ) { "Invalid tag for Diaspora Building Solutions" }
            }

            ProductOffering.BUILDING_CONSULTANCY -> {
                require(consultancySubtags.isNotEmpty()) { "At least one consultancy subtag is required for Building Consultancy" }
                require(productTag == ProductTag.BUILDING_CONSULTANCY) { "Invalid tag for Building Consultancy" }
            }

            ProductOffering.RENOVATIONS_REMODELING_REPAIRS -> {
                require(productTag == ProductTag.RENOVATIONS_REMODELING_REPAIRS) { "Invalid tag for Renovations, Remodeling & Repairs" }
            }
        }
    }
}