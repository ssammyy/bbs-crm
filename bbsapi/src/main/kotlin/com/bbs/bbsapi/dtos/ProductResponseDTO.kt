package com.bbs.bbsapi.dtos
import com.bbs.bbsapi.models.PortfolioFileType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ProductResponseDTO(
    @JsonProperty("id") val id: Long? = null,
    @JsonProperty("productTitle") val productTitle: String? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("fileType") val fileType: PortfolioFileType? = null,
    @JsonProperty("fileName") val fileName: String? = null,
    @JsonProperty("objectKey") val objectKey: String? = null,
    @JsonProperty("fileUrl") val fileUrl: String? = null,
    @JsonProperty("additionalFileUrls") val additionalFileUrls: List<String> = emptyList(),
    @JsonProperty("createdAt") val createdAt: LocalDateTime,
    @JsonProperty("productVp") val productVp: String? = null,
    @JsonProperty("productHook") val productHook: String? = null,
    @JsonProperty("videoUrl") val videoUrl: String? = null
)