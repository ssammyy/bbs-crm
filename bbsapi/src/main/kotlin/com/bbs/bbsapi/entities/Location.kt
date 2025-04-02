package com.bbs.bbsapi.entities

import jakarta.persistence.Embeddable

@Embeddable
data class Location(
    var isKenyan: Boolean, // True for Kenyan clients, False for international
    var county: String?, // Only required if isKenyan = true
    var country: String // Always required
)



