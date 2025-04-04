package com.bbs.bbsapi.repos

import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.FileMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FileRepository : JpaRepository<FileMetadata, Long> {
    fun findByClient(client: Client): List<FileMetadata>
    fun findByClientAndFileType(client: Client, fileType: FileType): FileMetadata?

}

