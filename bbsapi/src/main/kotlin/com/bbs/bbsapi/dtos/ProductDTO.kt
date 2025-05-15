package com.bbs.bbsapi.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductDTO(
    @JsonProperty("productTitle") val productTitle: String,
    @JsonProperty("description") val description: String
)