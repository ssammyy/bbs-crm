package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.ReportStatus
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
data class SiteReport(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val location: String = "",
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", unique = true)
    @JsonManagedReference
    val client: Client? = null,
    val soilType: String = "",
    val siteMeasurements: String = "",
    val topography: String = "",

    @ElementCollection
    @Column(name = "infrastructure")
    val infrastructure: List<String> = emptyList(),
    val notes: String? = null,
    val status: ReportStatus = ReportStatus.PENDING, // Default to PENDING
    val rejectionComments: String? = null //

)