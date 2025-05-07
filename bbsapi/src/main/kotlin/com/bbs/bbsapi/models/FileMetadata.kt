package com.bbs.bbsapi.models

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.enums.LocationType
import com.bbs.bbsapi.enums.PreferredContactMethod
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "files")
data class FileMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var fileType: FileType? = null,

    @Column(nullable = false)
    var fileName: String? = null,

    @Column(nullable = false)
    var objectKey: String? = null,

    @Column(nullable = false, length = 2000)
    val fileUrl: String? = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    val client: Client? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "preliminary_id", referencedColumnName = "id")
    val preliminary: Preliminary? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User? = null


) : BaseEntity() {
    constructor() : this(
        0, FileType.ID, "", "", "", null, null
    )
}