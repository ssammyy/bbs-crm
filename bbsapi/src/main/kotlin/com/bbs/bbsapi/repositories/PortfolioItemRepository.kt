package com.bbs.bbsapi.repositories

import com.bbs.bbsapi.models.PortfolioItem
import org.springframework.data.jpa.repository.JpaRepository

interface PortfolioItemRepository : JpaRepository<PortfolioItem, Long> {
}