package com.bbs.bbsapi.services

import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.FileMetadata
import com.bbs.bbsapi.models.Preliminary
import com.bbs.bbsapi.repos.FileRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.ByteArrayOutputStream
import java.time.Duration

@Service
class DigitalOceanService(
    @Value("\${digitalocean.spaces.endpoint}") private val endpoint: String,
    @Value("\${digitalocean.spaces.accessKey}") private val accessKey: String,
    @Value("\${digitalocean.spaces.secretKey}") private val secretKey: String,
    @Value("\${digitalocean.spaces.bucketName}") private val bucketName: String,
    private val fileMetadataRepository: FileRepository,
    private val fileRepository: FileRepository
) {
    private val s3Client: S3Client = S3Client.builder()
        .region(Region.US_EAST_1) // DigitalOcean Spaces uses any region, this is just a placeholder
        .endpointOverride(java.net.URI(endpoint))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build()

    /**
     * Upload a file to DigitalOcean Spaces
     */
    fun uploadFile(file: MultipartFile): String {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(file.originalFilename)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(
            putObjectRequest,
            software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                file.inputStream,
                file.inputStream.available().toLong()
            )
        )
        return "success"
    }

    fun getFileUrl(fileName: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(30))
            .getObjectRequest(getObjectRequest)
            .build()

        return presigner.presignGetObject(presignRequest).url().toString()
    }

    fun getFileContent(fileName: String): ByteArray {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build()

        val byteArrayOutputStream = ByteArrayOutputStream()
        s3Client.getObject(getObjectRequest).use { responseInputStream ->
            responseInputStream.copyTo(byteArrayOutputStream)
        }

        return byteArrayOutputStream.toByteArray()
    }

    fun getFileContentType(fileName: String): String {
        val headObjectRequest = HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build()
        val headObjectResponse = s3Client.headObject(headObjectRequest)
        return headObjectResponse.contentType()
    }


    private val presigner: S3Presigner = S3Presigner.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(java.net.URI(endpoint))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build()
    /**
     * Get a **pre-signed** URL for secure downloads
     */


    /**
     * Delete a file from Spaces
     */
    fun deleteFile(fileName: String) {
        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build()

        s3Client.deleteObject(deleteRequest)
    }

    fun getClientFiles(client: Client): ResponseEntity<List<Map<String, Any?>>> {
        val clientFiles = fileRepository.findByClient(client)
        val filesWithUrls = clientFiles.map { fileMetadata ->
            val presignedUrl = getFileUrl(fileMetadata.objectKey.toString())
            mapOf(
                "id" to fileMetadata.id,
                "fileType" to fileMetadata.fileType,
                "fileName" to fileMetadata.fileName,
                "fileUrl" to presignedUrl
            )
        }
        return ResponseEntity.ok(filesWithUrls)
    }
    fun getPreliminaryFiles(client: Client, preliminary: Preliminary): ResponseEntity<List<Map<String, Any?>>> {
        val clientFiles = fileRepository.findByClientAndPreliminary(client, preliminary)
        val filesWithUrls = clientFiles.map { fileMetadata ->
            val presignedUrl = getFileUrl(fileMetadata.objectKey.toString())
            mapOf(
                "id" to fileMetadata.id,
                "fileType" to fileMetadata.fileType,
                "fileName" to fileMetadata.fileName,
                "fileUrl" to presignedUrl
            )
        }
        return ResponseEntity.ok(filesWithUrls)
    }


    @Transactional
    fun updateMetadata(client: Client, file: MultipartFile, fileType: FileType): ResponseEntity<FileMetadata> {
        val fileMetadata = fileMetadataRepository.findByClientAndFileType(client, fileType)
        fileMetadata?.fileType = fileType
        fileMetadata?.objectKey = file.originalFilename.toString()
        fileMetadata?.fileName = file.originalFilename.toString()
        fileMetadata?.let { fileMetadataRepository.save(it) }
        return ResponseEntity.ok(fileMetadata)
    }
}
