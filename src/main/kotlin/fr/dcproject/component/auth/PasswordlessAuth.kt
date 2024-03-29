package fr.dcproject.component.auth

import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import fr.dcproject.common.email.Mailer
import fr.dcproject.component.auth.jwt.makeToken
import fr.dcproject.component.citizen.database.CitizenRepository
import fr.dcproject.component.citizen.database.CitizenWithEmail
import fr.dcproject.component.citizen.database.CitizenWithUserI
import io.ktor.http.URLBuilder

/**
 * Send a connexion link by email
 */
class PasswordlessAuth(
    private val mailer: Mailer,
    private val domain: String,
    private val citizenRepo: CitizenRepository
) {
    fun sendEmail(email: String, url: String) {
        val citizen = citizenRepo.findByEmail(email) ?: noEmail(email)
        sendEmail(citizen, url)
    }

    fun <C> sendEmail(citizen: C, url: String) where C : CitizenWithEmail, C : CitizenWithUserI {
        mailer.sendEmail {
            val token = citizen.user.makeToken()
            Mail(
                Email("passwordless-auth@$domain"),
                "Connection",
                Email(citizen.email),
                Content("text/plain", generateContent(token, url))
            ).apply {
                addContent(Content("text/html", generateHtmlContent(token, url)))
            }
        }
    }

    private fun generateHtmlContent(token: String, url: String): String? {
        val urlObject = URLBuilder(url)
        urlObject.parameters.append("token", token)
        return "Click <a href=\"${urlObject.buildString()}\">here</a> for connect to $domain"
    }

    private fun generateContent(token: String, url: String): String {
        val urlObject = URLBuilder(url)
        urlObject.parameters.append("token", token)
        return "Copy this link into your browser for connect to $domain: \n${urlObject.buildString()}"
    }

    class EmailNotFound(val email: String) : Exception() {
        override val message: String = "No Citizen with this email : $email"
    }

    private fun noEmail(email: String): Nothing = throw EmailNotFound(email)
}
