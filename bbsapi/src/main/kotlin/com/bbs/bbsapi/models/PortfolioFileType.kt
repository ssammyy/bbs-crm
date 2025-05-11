package com.bbs.bbsapi.models

import jakarta.persistence.*
import java.time.LocalDateTime

enum class PortfolioFileType {
    IMAGE, VIDEO
}

@Entity
@Table(name = "portfolio_items")
data class PortfolioItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var title: String? = null,

    @Column(nullable = false)
    var client: String? = null,

    @Column(nullable = false)
    var typeOfContract: String? = null,

    @Column(nullable = false, length = 2000)
    var description: String? = null,

    @Column(nullable = false)
    var location: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var fileType: PortfolioFileType? = null,

    @Column(nullable = false)
    var fileName: String? = null,

    @Column(nullable = false)
    var objectKey: String? = null,


    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)