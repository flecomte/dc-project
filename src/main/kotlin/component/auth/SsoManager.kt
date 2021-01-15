package fr.dcproject.component.auth

import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import fr.dcproject.JwtConfig
import fr.dcproject.component.citizen.CitizenBasicI
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.messages.Mailer
import io.ktor.http.*

/**
 * Send an email to the citizen with a link to automatically connect
 */
class SsoManager(
    private val mailer: Mailer,
    private val domain: String,
    private val citizenRepo: CitizenRepository
) {
    fun sendEmail(email: String, url: String) {
        val citizen = citizenRepo.findByEmail(email) ?: noEmail(email)
        sendEmail(citizen, url)
    }

    fun sendEmail(citizen: CitizenBasicI, url: String) {
        mailer.sendEmail {
            Mail(
                Email("sso@$domain"),
                "Connection",
                Email(citizen.email),
                Content("text/plain", generateContent(citizen, url))
            ).apply {
                addContent(Content("text/html", generateHtmlContent(citizen, url)))
            }
        }
    }

    /**
     * TODO pass token to the function to avoid double generations
     */
    private fun generateHtmlContent(citizen: CitizenBasicI, url: String): String? {
        val urlObject = URLBuilder(url)
        urlObject.parameters.append("token", JwtConfig.makeToken(citizen.user))
        return "Click <a href=\"${urlObject.buildString()}\">here</a> for connect to $domain"
    }

    /**
     * TODO pass token to the function to avoid double generations
     */
    private fun generateContent(citizen: CitizenBasicI, url: String): String {
        val urlObject = URLBuilder(url)
        urlObject.parameters.append("token", JwtConfig.makeToken(citizen.user))
        return "Copy this link into your browser for connect to $domain: \n${urlObject.buildString()}"
    }

    class EmailNotFound(val email: String) : Exception() {
        override val message: String = "No Citizen with this email : $email"
    }

    private fun noEmail(email: String): Nothing = throw EmailNotFound(email)
}