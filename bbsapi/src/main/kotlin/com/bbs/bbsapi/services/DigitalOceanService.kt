package com.bbs.bbsapi.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.time.Duration

@Service
class DigitalOceanService(
    @Value("\${digitalocean.spaces.endpoint}") private val endpoint: String,
    @Value("\${digitalocean.spaces.accessKey}") private val accessKey: String,
    @Value("\${digitalocean.spaces.secretKey}") private val secretKey: String,
    @Value("\${digitalocean.spaces.bucketName}") private val bucketName: String
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

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.inputStream, file.inputStream.available().toLong()))

        return "$endpoint/$bucketName/$file.originalFilename"
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
    fun getFileUrl(fileName: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(30)) // URL expires in 30 minutes
            .getObjectRequest(getObjectRequest) // ✅ Correct method
            .build()

        return presigner.presignGetObject(presignRequest).url().toString()
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
}
