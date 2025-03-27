package com.bbs.bbsapi.models

import jakarta.persistence.*
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(EntityListener::class)
abstract class BaseEntity {

    @Column(name = "created_on", nullable = false, updatable = false)
    var createdOn: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_on", nullable = false)
    var updatedOn: LocalDateTime = LocalDateTime.now()

    @Column(name = "created_by", nullable = true)
    var createdBy: String? = null

    @Column(name = "updated_by", nullable = true)
    var updatedBy: String? = null

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false
}
