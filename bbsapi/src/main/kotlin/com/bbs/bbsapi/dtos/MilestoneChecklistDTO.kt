package com.bbs.bbsapi.dtos

data class MilestoneItemDTO(
    val name: String,
    val completed: Boolean
)

data class MilestoneChecklistDTO(
    val clientId: Long,
    val clientName: String?,
    val siteLocation: String?,
    val milestones: List<MilestoneItemDTO?>
) 