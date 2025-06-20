package com.bbs.bbsapi.dtos

import java.math.BigDecimal

data class ContractProgressUpdateDTO(
    val moneyUsedSoFar: BigDecimal,
    val lastReportFileUrl: String?
) 