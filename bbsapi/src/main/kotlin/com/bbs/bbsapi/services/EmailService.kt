package com.bbs.bbsapi.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Service

@Service
class EmailService {
    @Value("\${spring.mail.username}")
    private val username: String? = null

    @Value("\${spring.mail.password}")
    private val password: String? = null

    val javaMailSender: JavaMailSender
        get() {
            val mailSender = JavaMailSenderImpl()
            mailSender.host = "smtp.gmail.com"
            mailSender.port = 587

            mailSender.username = username
            mailSender.password = password

            val props = mailSender.javaMailProperties
            props["mail.transport.protocol"] = "smtp"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.debug"] = "true"

            return mailSender
        }

    fun sendEmail(to: String?, subject: String?, body: String?) {
        val sender = javaMailSender
        val message = SimpleMailMessage()
        message.from = username
        message.setTo(to)
        message.subject = subject
        message.text = body
        sender.send(message)
    }

    fun sendConfirmationEmail(to: String, token: String) {
        val link = "http://localhost:4200/confirm-email?token=$token"
        val message = "Click the following link to confirm your email: $link"
        sendEmail(to, "Confirm your email", message)
    }

    fun sendResetPasswordEmail(to: String, token: String) {
        val link = "http://localhost:4200/reset-password?token=$token"
        val message = "Click to reset your password: $link"
        sendEmail(to, "Password Reset", message)
    }
}