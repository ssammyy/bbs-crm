package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.models.FileMetadata
import com.bbs.bbsapi.repos.ClientRepo
import com.bbs.bbsapi.repos.FileRepository
import com.bbs.bbsapi.services.DigitalOceanService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Controller
@RequestMapping(value = ["/api/files"])
class UploadFile(
    private val clientRepository: ClientRepo,
    private val digitalOceanService: DigitalOceanService,
    private val fileRepository: FileRepository
){
    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("clientId") clientId: Long,
        @RequestParam("fileType") fileType: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<*> {
        val client = clientRepository.findByIdNumber(clientId) ?: return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body("Client with ID $clientId not found")
        // Convert string to enum safely
        val fileTypeEnum = try {
            FileType.valueOf(fileType.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body("Invalid file type")
        }

        // Upload to DigitalOcean Spaces
        val fileUrl = digitalOceanService.uploadFile(file)

        // Save file metadata
        val fileMetadata = FileMetadata(
            fileType = fileTypeEnum,
            fileName = file.originalFilename ?: "unknown",
            fileUrl = fileUrl,
            client = client
        )

        fileRepository.save(fileMetadata)

        return ResponseEntity.ok(mapOf("message" to "File uploaded successfully"))
    }

    @GetMapping("/content/{fileName}")
    fun getFileContent(@PathVariable fileName: String): ResponseEntity<Any> {
        return try {
            val fileContent = digitalOceanService.getFileContent(fileName)
            val contentType = digitalOceanService.getFileContentType(fileName)

            if (contentType.startsWith("image/")) {
                ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileContent)
            } else if (contentType == "application/pdf") {
                val encodedPdf = Base64.getEncoder().encodeToString(fileContent);

                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mapOf("pdfBase64" to encodedPdf))

            } else {
                ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(String(fileContent))
            }

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving file: ${e.message}")
        }
    }

    @GetMapping("/url/{fileName}")
    fun getFileUrl(@PathVariable fileName: String): ResponseEntity<String> {
        val fileUrl = digitalOceanService.getFileUrl(fileName)
        return ResponseEntity.ok(fileUrl.toString())
    }

    @DeleteMapping("/delete/{fileName}")
    fun deleteFile(@PathVariable fileName: String): ResponseEntity<String> {
        digitalOceanService.deleteFile(fileName)
        return ResponseEntity.ok("File deleted successfully")
    }

}
