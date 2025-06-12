package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.services.EmailService
import com.postmarkapp.postmark.client.ApiClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.util.logging.Logger
import java.net.InetAddress
import java.net.UnknownHostException

@RestController
@RequestMapping("/api/test/email")
class EmailTestController(
    private val emailService: EmailService
) {
    private val logger = Logger.getLogger(EmailTestController::class.java.name)

    @Value("\${postmark.api.token}")
    private lateinit var postmarkApiToken: String

    @Value("\${spring.mail.username}")
    private lateinit var fromEmail: String

    private val postmarkClient: ApiClient by lazy {
        try {
            logger.info("Initializing Postmark ApiClient with token: ${postmarkApiToken.take(4)}****")
            // Try to resolve the hostname first
            try {
                val hostname = "api.postmarkapp.com"
                val ipAddress = InetAddress.getByName(hostname).hostAddress
                logger.info("Resolved $hostname to IP: $ipAddress")
            } catch (e: UnknownHostException) {
                logger.warning("Could not resolve api.postmarkapp.com, will try direct IP")
            }

            // Use direct IP address for Postmark API
            val baseUrl = "https://104.18.6.6" // Postmark API IP address
            val headers = mutableMapOf<String, Any>(
                "X-Postmark-Server-Token" to postmarkApiToken,
                "Host" to "api.postmarkapp.com" // Add Host header for proper routing
            )
            ApiClient(baseUrl, headers)
        } catch (e: Exception) {
            logger.severe("Failed to initialize Postmark client: ${e.message}")
            throw e
        }
    }

    @PostMapping("/send")
    fun testSendEmail(
        @RequestParam to: String,
        @RequestParam subject: String,
        @RequestParam body: String,
        @RequestParam(required = false) file: MultipartFile?
    ): ResponseEntity<Map<String, String>> {
        try {
            val attachment = file?.let {
                emailService.createAttachment(
                    fileName = it.originalFilename ?: "attachment",
                    inputStream = ByteArrayInputStream(it.bytes),
                    contentType = it.contentType ?: "application/octet-stream"
                )
            }

            emailService.sendEmail(
                to = to,
                subject = subject,
                body = body,
                attachment = attachment
            )

            return ResponseEntity.ok(mapOf<String, String>(
                "status" to "success",
                "message" to "Email sent successfully"
            ))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf<String, String>(
                "status" to "error",
                "message" to (e.message ?: "Failed to send email")
            ))
        }
    }

    @PostMapping("/confirm")
    fun testConfirmationEmail(
        @RequestParam to: String,
        @RequestParam token: String
    ): ResponseEntity<Map<String, String>> {
        try {
            emailService.sendConfirmationEmail(
                to = to,
                token = token
            )

            return ResponseEntity.ok(mapOf<String, String>(
                "status" to "success",
                "message" to "Confirmation email sent successfully"
            ))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf<String, String>(
                "status" to "error",
                "message" to (e.message ?: "Failed to send confirmation email")
            ))
        }
    }

    @PostMapping("/reset-password")
    fun testResetPasswordEmail(
        @RequestParam to: String,
        @RequestParam token: String
    ): ResponseEntity<Map<String, String>> {
        try {
            emailService.sendResetPasswordEmail(
                to = to,
                token = token
            )

            return ResponseEntity.ok(mapOf<String, String>(
                "status" to "success",
                "message" to "Password reset email sent successfully"
            ))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf<String, String>(
                "status" to "error",
                "message" to (e.message ?: "Failed to send password reset email")
            ))
        }
    }

    @PostMapping("/send-postmark")
    fun testSendEmailWithPostmark(
        @RequestParam to: String,
        @RequestParam subject: String,
        @RequestParam body: String
    ): ResponseEntity<Map<String, String>> {
        try {
            logger.info("Attempting to send email via Postmark to: $to")
            
            // Validate email addresses
            if (!isValidEmail(to) || !isValidEmail(fromEmail)) {
                throw IllegalArgumentException("Invalid email address format")
            }

            val message = com.postmarkapp.postmark.client.data.model.message.Message(
                fromEmail,
                to,
                subject,
                null
            ).apply {
                htmlBody = """
                    <html>
                    <body>
                        <h2>$subject</h2>
                        <p>$body</p>
                        <p>This is a test email sent via Postmark API.</p>
                    </body>
                    </html>
                """.trimIndent()
            }

            logger.info("Sending message via Postmark: From=$fromEmail, To=$to, Subject=$subject")
            val response = postmarkClient.deliverMessage(message)
            logger.info("Email sent successfully via Postmark to: $to, Message ID: ${response.messageId}")

            return ResponseEntity.ok(mapOf<String, String>(
                "status" to "success",
                "message" to "Email sent successfully via Postmark",
                "messageId" to (response.messageId ?: "unknown")
            ))
        } catch (e: Exception) {
            logger.severe("Failed to send email via Postmark: ${e.message}")
            e.printStackTrace()
            return ResponseEntity.badRequest().body(mapOf<String, String>(
                "status" to "error",
                "message" to "Failed to send email via Postmark: ${e.message}",
                "errorType" to e.javaClass.simpleName,
                "stackTrace" to e.stackTraceToString()
            ))
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)\$"))
    }
} 