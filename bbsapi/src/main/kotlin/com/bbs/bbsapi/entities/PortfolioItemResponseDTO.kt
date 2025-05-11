package com.bbs.bbsapi.entities

import com.bbs.bbsapi.models.PortfolioFileType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class PortfolioItemResponseDTO(
    @JsonProperty("id") val id: Long?,
    @JsonProperty("title") val title: String?,
    @JsonProperty("client") val client: String?,
    @JsonProperty("typeOfContract") val typeOfContract: String?,
    @JsonProperty("description") val description: String?,
    @JsonProperty("location") val location: String?,
    @JsonProperty("fileType") val fileType: PortfolioFileType?,
    @JsonProperty("fileName") val fileName: String?,
    @JsonProperty("objectKey") val objectKey: String?,
    @JsonProperty("fileUrl") val fileUrl: String?,
    @JsonProperty("createdAt") val createdAt: LocalDateTime?
)