package com.bbs.bbsapi.repos

import com.bbs.bbsapi.models.FileMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FileRepository : JpaRepository<FileMetadata, Long> {

}

