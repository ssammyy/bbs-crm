package com.bbs.bbsapi.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductDTO(
    @JsonProperty("productTitle") val productTitle: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("productVp") val productVp: String,
    @JsonProperty("productHook") val productHook: String,
    @JsonProperty("videoUrl") val videoUrl: String,
)