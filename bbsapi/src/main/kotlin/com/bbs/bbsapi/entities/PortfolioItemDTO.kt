package com.bbs.bbsapi.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class PortfolioItemDTO(
    @JsonProperty("title") val title: String,
    @JsonProperty("client") val client: String,
    @JsonProperty("typeOfContract") val typeOfContract: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("location") val location: String
)