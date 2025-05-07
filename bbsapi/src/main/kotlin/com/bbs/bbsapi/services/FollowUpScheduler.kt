package com.bbs.bbsapi.services

import com.bbs.bbsapi.services.ClientCommitmentService
import com.bbs.bbsapi.services.EmailService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class FollowUpScheduler(
    private val clientCommitmentService: ClientCommitmentService,
    private val emailService: EmailService
) {

    private val internalUserEmail = "sales@bbsltd.co.ke"

    @Scheduled(cron = "0 0 9 * * ?")
    fun checkFollowUps() {
        val currentDate = LocalDateTime.now()
        val clientCommitmentsToFollowUp = clientCommitmentService.getClientCommitmentsToFollowUp(currentDate)

        clientCommitmentsToFollowUp.forEach { clientCommitment ->
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val formattedDate = clientCommitment.followUpDate.format(formatter)

            // Send email to client
//            emailService.sendFollowUpEmail(clientCommitment.email, clientCommitment.name, formattedDate)
//
//            // Notify internal user
//            emailService.sendInternalNotification(
//                internalUserEmail,
//                clientCommitment.name,
//                clientCommitment.email,
//                formattedDate
//            )

            // Mark client commitment as contacted
            clientCommitmentService.markAsContacted(clientCommitment)
        }
    }
}