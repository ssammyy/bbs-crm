package com.bbs.bbsapi.services

import com.bbs.bbsapi.models.Notification
import com.bbs.bbsapi.models.User
import com.bbs.bbsapi.repositories.NotificationRepository
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Attachments
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Base64
import java.util.logging.Logger

@Service
class EmailService(
    private val notificationRepository: NotificationRepository
) {

    @Value("\${sendgrid.api.key}")
    private lateinit var sendgridApiKey: String

    @Value("\${spring.mail.from}")
    private lateinit var fromEmail: String

    @Value("\${client.uri}")
    private lateinit var siteUrl: String

    private val logger = Logger.getLogger(EmailService::class.java.name)

    private val sendGrid: SendGrid by lazy {
        logger.info("Initializing SendGrid client")
        SendGrid(sendgridApiKey)
    }

    data class EmailAttachment(
        val fileName: String,
        val content: ByteArray,
        val contentType: String
    )

    fun sendEmail(
        to: String?,
        subject: String?,
        body: String?,
        user: User? = null,
        attachment: EmailAttachment? = null
    ) {
        val recipient = user?.email ?: to
        if (recipient == null) {
            throw IllegalArgumentException("Recipient email must be provided")
        }

        logger.info("Preparing to send email to $recipient with subject: $subject")

        val from = Email(fromEmail)
        val toEmail = Email(recipient)
        val content = Content("text/html", """
            <html>
            <body>
                <h2>$subject</h2>
                <p>$body</p>
                <p><a href="$siteUrl">Visit Portal</a></p>
                <p>This is an automated message, please do not reply to this email.</p>
            </body>
            </html>
        """.trimIndent())

        val mail = Mail(from, subject, toEmail, content)

        // Add attachment if provided
        attachment?.let {
            logger.info("Adding attachment: ${it.fileName}, size: ${it.content.size} bytes")
            val attachments = Attachments()
            attachments.content = Base64.getEncoder().encodeToString(it.content)
            attachments.type = it.contentType
            attachments.filename = it.fileName
            attachments.disposition = "attachment"
            mail.addAttachments(attachments)
        }

        try {
            val request = Request()
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()

            val response: Response = sendGrid.api(request)
            logger.info("Email sent successfully to $recipient with status code: ${response.statusCode}")

            // Create notification if user is provided
            user?.let {
                val notification = Notification(
                    user = it,
                    title = subject ?: "",
                    message = body ?: "",
                    type = "EMAIL",
                    isRead = false
                )
                notificationRepository.save(notification)
            }
        } catch (e: Exception) {
            logger.severe("Failed to send email to $recipient: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Failed to send email: ${e.message}", e)
        }
    }

    // Helper function to create EmailAttachment from InputStream
    fun createAttachment(fileName: String, inputStream: InputStream, contentType: String): EmailAttachment {
        val outputStream = ByteArrayOutputStream()
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return EmailAttachment(fileName, outputStream.toByteArray(), contentType)
    }

    fun sendConfirmationEmail(to: String, token: String, user: User? = null, attachment: EmailAttachment? = null) {
        val link = "$siteUrl/confirm-email?token=$token"
        val htmlContent = """
            <html>
            <body>
                <h2>Welcome to BBS!</h2>
                <p>Thank you for registering. Please confirm your email address by clicking the button below, then set a password for login to the system.</p>   
                <p><a href="$link" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Confirm Email Address</a></p>
                <p>If the button doesn't work, you can also copy and paste this link into your browser:</p>
                <p>$link</p>
                <p><a href="$siteUrl">Visit Portal</a></p>
                <p>This is an automated message, please do not reply to this email.</p>
            </body>
            </html>
        """.trimIndent()

        sendEmail(to, "Confirm your email", htmlContent, user, attachment)
    }

    fun sendResetPasswordEmail(to: String, token: String, user: User? = null, attachment: EmailAttachment? = null) {
        val link = "$siteUrl/reset-password?token=$token"
        val htmlContent = """
            <html>
            <body>
                <h2>Password Reset Request</h2>
                <p>We received a request to reset your password. Click the button below to create a new password:</p>
                <p><a href="$link" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Reset Password</a></p>
                <p>If the button doesn't work, you can also copy and paste this link into your browser:</p>
                <p>$link</p>
                <p>If you didn't request a password reset, you can safely ignore this email.</p>
                <p><a href="$siteUrl">Visit Portal</a></p>
                <p>This is an automated message, please do not reply to this email.</p>
            </body>
            </html>
        """.trimIndent()

        sendEmail(to, "Password Reset", htmlContent, user, attachment)
    }
}