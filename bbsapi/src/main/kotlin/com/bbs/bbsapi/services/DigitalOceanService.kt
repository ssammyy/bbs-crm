package com.bbs.bbsapi.services

import com.bbs.bbsapi.enums.FileType
import com.bbs.bbsapi.models.Client
import com.bbs.bbsapi.models.FileMetadata
import com.bbs.bbsapi.models.Preliminary
import com.bbs.bbsapi.repos.FileRepository
import org.slf4j.LoggerFactory
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
import java.time.Instant
import java.time.format.DateTimeFormatter

@Service
class DigitalOceanService(
    @Value("\${digitalocean.spaces.endpoint}") private val endpoint: String,
    @Value("\${digitalocean.spaces.accessKey}") private val accessKey: String,
    @Value("\${digitalocean.spaces.secretKey}") private val secretKey: String,
    @Value("\${digitalocean.spaces.bucketName}") private val bucketName: String,
    private val fileMetadataRepository: FileRepository,
    private val fileRepository: FileRepository
) {
    private val logger = LoggerFactory.getLogger(DigitalOceanService::class.java)

    private val s3Client: S3Client = S3Client.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(java.net.URI(endpoint))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build()

    private val presigner: S3Presigner = S3Presigner.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(java.net.URI(endpoint))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build()

    /**
     * Upload a file to DigitalOcean Spaces and return the objectKey and direct URL
     */
    fun uploadFile(file: MultipartFile): Pair<String, String> {
        val fileName = file.originalFilename ?: throw IllegalArgumentException("File name is missing")
        val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "").replace("-", "")
        val objectKey = "portfolio/${timestamp}_$fileName"

        logger.info("Uploading file to bucket: $bucketName, objectKey: $objectKey")

        try {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(file.contentType)
                .build()

            val putResponse = s3Client.putObject(
                putObjectRequest,
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                    file.inputStream,
                    file.size
                )
            )

            // Verify upload
            val headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build()
            s3Client.headObject(headObjectRequest)

            logger.info("Upload successful for objectKey: $objectKey, ETag: ${putResponse.eTag()}")

            val fileUrl = getPublicFileUrl(objectKey)
            return Pair(objectKey, fileUrl)
        } catch (e: Exception) {
            logger.error("Failed to upload file to Spaces: ${e.message}", e)
            throw IllegalStateException("File upload failed: ${e.message}", e)
        }
    }

    /**
     * Get a pre-signed URL for secure downloads
     */
    fun getFileUrl(fileName: String): String {
        logger.info("Generating presigned URL for bucket: $bucketName, key: $fileName")
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

    /**
     * Get a direct URL for public access
     */
    fun getPublicFileUrl(fileName: String): String {
        logger.info("Generating direct URL for bucket: $bucketName, key: $fileName")
        return "https://$bucketName.${endpoint.replace("https://", "")}/$fileName"
    }

    /**
     * Get file content as a byte array
     */
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

    /**
     * Get file content type
     */
    fun getFileContentType(fileName: String): String {
        val headObjectRequest = HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build()
        val headObjectResponse = s3Client.headObject(headObjectRequest)
        return headObjectResponse.contentType()
    }

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

    /**
     * Get files for a client
     */
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

    /**
     * Get files for a client and preliminary
     */
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

    /**
     * Update file metadata
     */
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