package com.bbs.bbsapi

import com.bbs.bbsapi.repositories.NotificationRepository
import com.bbs.bbsapi.services.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BbsapiApplication

fun main(args: Array<String>) {
//     val emailService = EmailService(
//     )
//    emailService.sendEmail(
//        "samuikumbu@gmail.com",
//       "Test",
//        "test@test.com",
//    )

    runApplication<BbsapiApplication>(*args)
}


