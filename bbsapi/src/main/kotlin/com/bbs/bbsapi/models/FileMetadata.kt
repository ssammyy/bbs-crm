package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.enums.LocationType
import com.bbs.bbsapi.enums.PreferredContactMethod
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "files")
data class FileMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val fileType: FileType,
    val fileName: String,
    val fileUrl: String,
    val objectKey: String,
    val version: Int = 1,
    val versionNotes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @ManyToOne
    val client: Client,
    @ManyToOne
    val preliminary: Preliminary? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User? = null
) : BaseEntity() {
    constructor() : this(
        id = 0,
        fileType = FileType.ID,
        fileName = "",
        fileUrl = "",
        objectKey = "",
        client = Client(),
        preliminary = null,
        user = null
    )
}