package com.bbs.bbsapi.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "product_title", nullable = false)
    var productTitle: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "file_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var fileType: PortfolioFileType? = null,

    @Column(name = "file_name", nullable = false)
    var fileName: String? = null,

    @Column(name = "object_key", nullable = false)
    var objectKey: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name= "productVp", columnDefinition = "TEXT")
    var productVp: String? = null,
    @Column(name= "productHook", columnDefinition = "TEXT")
    var productHook: String? = null,
    @Column(name= "videoUrl", nullable = false)
    var videoUrl: String? = null,

)



