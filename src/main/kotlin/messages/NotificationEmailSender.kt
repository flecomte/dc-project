package fr.dcproject.messages

import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import fr.dcproject.entity.*
import fr.postgresjson.entity.immutable.UuidEntityI
import java.util.*
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.Article as ArticleRepository

class NotificationEmailSender(
    private val mailer: Mailer,
    private val domain: String,
    private val citizenRepo: CitizenRepository,
    private val articleRepo: ArticleRepository
) {
    fun sendEmail(follow: FollowSimple<out TargetRef, CitizenRef>) {
        val citizen = citizenRepo.findById(follow.createdBy.id) ?: noCitizen(follow.createdBy.id)
        val target = when (follow.target.reference) {
            "article" ->
                articleRepo.findById(follow.target.id) ?: noTarget(follow.target.id)
            else -> noTarget(follow.target.id)
        }
        val subject = when (follow.target.reference) {
            "article" -> """New version for article "${target.title}""""
            else -> "Notification"
        }
        mailer.sendEmail {
            Mail(
                Email("notification@$domain"),
                subject,
                Email(citizen.email),
                Content("text/plain", generateContent(citizen, target))
            ).apply {
                addContent(Content("text/html", generateHtmlContent(citizen, target)))
            }
        }
    }

    private fun generateHtmlContent(citizen: CitizenBasicI, target: UuidEntityI): String? {
        return when (target) {
            is Article -> """
                Hello ${citizen.name.getFullName()},<br/>
                The article "${target.title}" was updated, check it <a href="http://$domain/articles/${target.id}">here</a>
            """.trimIndent()
            else -> noTarget(target.id)
        }
    }

    private fun generateContent(citizen: CitizenBasicI, target: UuidEntityI): String {
        return when (target) {
            is Article -> """
                Hello ${citizen.name.getFullName()},
                The article "${target.title}" was updated, check it here: http://$domain/articles/${target.id}
            """.trimIndent()
            else -> noTarget(target.id)
        }
    }

    class NoCitizen(message: String) : Exception(message)
    class NoTarget(message: String) : Exception(message)

    private fun noCitizen(id: UUID): Nothing = throw NoCitizen("No Citizen with this id : $id")
    private fun noTarget(id: UUID): Nothing = throw NoTarget("No Target with this id : $id")
}