package fr.dcproject.messages

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import java.io.IOException

class Mailer (
    private val key: String
) {
    fun sendEmail(from: String, to: String, content: String, subject: String): Boolean {
        val mail = Mail(
            Email(from),
            subject,
            Email(to),
            Content("text/plain", content)
        )

        val sg = SendGrid(key)
        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response = sg.api(request)
            return response.statusCode == 202
        } catch (ex: IOException) {
            throw ex
        }
    }
}