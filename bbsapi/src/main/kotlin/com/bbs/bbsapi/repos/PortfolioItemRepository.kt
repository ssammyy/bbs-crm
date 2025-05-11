package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.PortfolioItem
import org.springframework.data.jpa.repository.JpaRepository

interface PortfolioItemRepository : JpaRepository<PortfolioItem, Long> {
}