package com.bbs.bbsapi.repos

import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.FileMetadata
import com.bbs.bbsapi.models.Preliminary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FileRepository : JpaRepository<FileMetadata, Long> {
    fun findByClient(client: Client): List<FileMetadata>
    fun findByClientAndPreliminary(client: Client, preliminary: Preliminary): List<FileMetadata>
    fun findFirstByClientAndFileType(client: Client, fileType: FileType): FileMetadata?
    fun countByFileType(type: FileType): Long
    fun countByClientId(clientId: Long): Long
    fun countByClientIdAndFileType(clientId: Long, type: FileType): Long

}

