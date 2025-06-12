package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.enums.ClientStage
import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.enums.PreliminaryStatus
import com.bbs.bbsapi.models.FileMetadata
import com.bbs.bbsapi.models.Preliminary
import com.bbs.bbsapi.repositories.ClientRepository
import com.bbs.bbsapi.repositories.FileRepository
import com.bbs.bbsapi.repositories.PreliminaryRepository
import com.bbs.bbsapi.repositories.UserRepository
import com.bbs.bbsapi.services.ClientService
import com.bbs.bbsapi.services.DigitalOceanService
import org.apache.coyote.BadRequestException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.Base64

@Controller
@RequestMapping(value = ["/api/files"])
class UploadFile(
    private val clientRepository: ClientRepository,
    private val digitalOceanService: DigitalOceanService,
    private val fileRepository: FileRepository,
    private val clientService: ClientService,
    private val userRepository: UserRepository,
    private val clientRepo: ClientRepository,
    private val preliminaryRepository: PreliminaryRepository
) {
    @PostMapping("/upload")
    @Transactional
    fun uploadFile(
        @RequestParam(required = false) clientId: Long?,
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) preliminaryId: Long?,
        @RequestParam("fileType") fileType: String,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false) versionNotes: String?
    ): ResponseEntity<*> {
        var preliminary: Preliminary? = null
        if (clientId != null) {
            val client = clientRepository.findById(clientId).orElseThrow {
                throw BadRequestException("Client not found")
            }
            val fileTypeEnum = try {
                FileType.valueOf(fileType.uppercase())
            } catch (e: IllegalArgumentException) {
                return ResponseEntity.badRequest().body("Invalid file type")
            }
            if(preliminaryId != null) {
                preliminary = preliminaryRepository.findById(preliminaryId).orElseThrow { IllegalArgumentException("Preliminary not found") }
            }

            if(fileTypeEnum=== FileType.COUNTY_INVOICE){

                preliminary?.status= PreliminaryStatus.PENDING_CLIENT_FACILITATION_PAYMENT
                preliminary?.let { preliminaryRepository.save(it) }
            }

            // Upload file to storage
            val (objectKey, fileUrl) = digitalOceanService.uploadFile(file)

            // Create file metadata with versioning
            val existingFile = fileRepository.findFirstByClientAndFileType(client, fileTypeEnum)
            val version = existingFile?.version?.plus(1) ?: 1

            val fileMetadata = FileMetadata(
                id = 0,
                fileType = fileTypeEnum,
                fileName = file.originalFilename ?: "",
                fileUrl = fileUrl,
                objectKey = objectKey,
                version = version,
                versionNotes = versionNotes,
                approved = fileTypeEnum != FileType.PRELIMINARY,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                client = client,
                preliminary = preliminary,
                user = null
            )

            fileRepository.save(fileMetadata)



            return ResponseEntity.ok(mapOf(
                "message" to "File uploaded successfully",
                "fileUrl" to fileUrl,
                "version" to version
            ))
        }

        if (userId != null) {
            val user = userRepository.findById(userId).orElseThrow {
                throw BadRequestException("User not found")
            }
            val fileTypeEnum = try {
                FileType.valueOf(fileType.uppercase())
            } catch (e: IllegalArgumentException) {
                return ResponseEntity.badRequest().body("Invalid file type")
            }

            // Upload file to storage
            val (objectKey, fileUrl) = digitalOceanService.uploadFile(file)

            val fileMetadata = FileMetadata(
                id = 0,
                fileType = fileTypeEnum,
                fileName = file.originalFilename ?: "",
                fileUrl = fileUrl,
                objectKey = objectKey,
                version = 1,
                versionNotes = versionNotes,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                client = null,
                preliminary = null,
                user = user
            )
            fileRepository.save(fileMetadata)

            return ResponseEntity.ok(mapOf(
                "message" to "File uploaded successfully",
                "fileUrl" to fileUrl,
                "version" to 1
            ))
        }
        return ResponseEntity.badRequest().body("Either clientId or userId must be provided")
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
                val encodedPdf = Base64.getEncoder().encodeToString(fileContent)
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

    @GetMapping("/{clientId}/client-files")
    fun getFiles(@PathVariable clientId: Long): ResponseEntity<List<Map<String, Any?>>> {
        val client = clientRepository.findById(clientId)
            .orElseThrow { RuntimeException("Client with ID $clientId not found") }
        return digitalOceanService.getClientFiles(client)
    }

    @PostMapping("/{clientId}/{fileType}/{fileId}/update")
    fun updateFile(
        @PathVariable clientId: Long,
        @PathVariable fileType: FileType,
        @PathVariable fileId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false) versionNotes: String?
    ): ResponseEntity<FileMetadata> {
        val client = clientRepository.findById(clientId)
            .orElseThrow { RuntimeException("Client with ID $clientId not found") }
        
        val (objectKey, fileUrl) = digitalOceanService.uploadFile(file)
        return digitalOceanService.updateMetadata(client, file, fileType,  fileId, versionNotes)
    }

    @GetMapping("/licenses")
    fun getLicenseFiles(): ResponseEntity<List<Map<String, Any?>>> {
        val licenseTypes = listOf(
            FileType.STRUCTURAL_ENGINEER_LICENSE,
            FileType.ARCHITECT_LICENSE,
            FileType.NCA_CONTRACTOR_LICENSE
        )

        val files = fileRepository.findByFileTypeIn(licenseTypes)
            .map { file ->
                mapOf(
                    "id" to file.id,
                    "fileType" to file.fileType.name,
                    "fileName" to file.fileName,
                    "fileUrl" to file.fileUrl,
                    "version" to file.version,
                    "createdAt" to file.createdAt,
                    "updatedAt" to file.updatedAt,
                    "userId" to file.user?.id,
                    "username" to file.user?.username,
                    "userEmail" to file.user?.email,
                    "userRole" to file.user?.role?.name
                )
            }

        return ResponseEntity.ok(files)
    }
}
