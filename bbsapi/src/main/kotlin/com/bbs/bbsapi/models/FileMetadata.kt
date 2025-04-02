package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.FileType
import jakarta.persistence.*

@Entity
@Table(name = "files")
data class FileMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val fileType: FileType,

    @Column(nullable = false)
    val fileName: String,

    @Column(nullable = false)
    val fileUrl: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    val client: Client
): BaseEntity()
