package fr.dcproject.messages

import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import fr.dcproject.JwtConfig
import io.ktor.http.URLBuilder
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.repository.Citizen as CitizenRepository

class SsoManager (
    private val mailer: Mailer,
    private val domain: String,
    private val citizenRepo: CitizenRepository
) {
    fun sendMail(email: String, url: String) {
        val citizen = citizenRepo.findByEmail(email) ?: error("No Citizen with this email")
        mailer.sendEmail {
            Mail(
                Email("sso@$domain"),
                "Connection",
                Email(email),
                Content("text/plain", generateContent(citizen, url))
            ).apply {
                addContent(Content("text/html", generateHtmlContent(citizen, url)))
            }
        }
    }

    private fun generateHtmlContent(citizen: CitizenEntity, url: String): String? {
        val urlObject = URLBuilder(url)
        urlObject.parameters.append("token", JwtConfig.makeToken(citizen.user ?: error("Citizen must have User")))
        return "Click <a href=\"${urlObject.buildString()}\">here</a> for connect to $domain"
    }

    private fun generateContent(citizen: CitizenEntity, url: String): String {
        val urlObject = URLBuilder(url)
        urlObject.parameters.append("token", JwtConfig.makeToken(citizen.user ?: error("Citizen must have User")))
        return "Copy this link into your browser for connect to $domain: \n${urlObject.buildString()}"
    }
}