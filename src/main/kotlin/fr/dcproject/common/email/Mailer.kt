package fr.dcproject.common.email

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import java.io.IOException

class Mailer(
    private val key: String
) {
    fun sendEmail(action: () -> Mail): Boolean {
        val mail = action()

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
