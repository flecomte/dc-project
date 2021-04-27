package fr.dcproject.component.notification.email

import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import fr.dcproject.common.email.Mailer
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.citizen.database.Citizen
import fr.dcproject.component.citizen.database.CitizenRepository
import fr.dcproject.component.follow.database.FollowForView
import fr.dcproject.component.notification.email.content.ArticleNotificationEmailContent
import fr.dcproject.component.notification.email.content.CitizenNotificationEmailContent
import java.util.UUID

/**
 * Send notification email on the follower
 */
class NotificationEmailSender(
    private val mailer: Mailer,
    private val domain: String,
    private val citizenRepo: CitizenRepository,
    private val articleRepo: ArticleRepository
) {
    /**
     * Send the Notification Email to the follower user
     */
    fun sendEmail(follow: FollowForView<out TargetRef>) {
        val citizen = citizenRepo.findById(follow.createdBy.id) ?: noCitizen(follow.createdBy.id)

        /**
         * Find the complete target entity by its ID according to its reference
         */
        val target = when (follow.target.reference) {
            "article" -> articleRepo.findById(follow.target.id) ?: noTarget(follow.target.id)
            "citizen" -> citizenRepo.findById(follow.target.id) ?: noTarget(follow.target.id)
            else -> noTarget(follow.target.id)
        }

        /**
         * Find content of the email according to the target type
         */
        val content = when (target) {
            is ArticleForView -> ArticleNotificationEmailContent(citizen, target, domain)
            is Citizen -> CitizenNotificationEmailContent(citizen, target, domain)
            else -> noTargetTypeImplementation(follow.target.reference)
        }

        /* Send email */
        mailer.sendEmail {
            Mail(
                Email("notification@$domain"),
                content.subject,
                Email(citizen.email),
                Content("text/plain", content.content)
            ).apply {
                addContent(Content("text/html", content.contentHtml))
            }
        }
    }

    class NoCitizen(message: String) : Exception(message)
    class NoTarget(message: String) : Exception(message)
    class NoTargetTypeImplement(message: String) : Exception(message)

    private fun noCitizen(id: UUID): Nothing = throw NoCitizen("No Citizen with this id : $id")
    private fun noTarget(id: UUID): Nothing = throw NoTarget("No Target with this id : $id")
    private fun noTargetTypeImplementation(type: String): Nothing = throw NoTargetTypeImplement("No Target type implemented: $type")
}
