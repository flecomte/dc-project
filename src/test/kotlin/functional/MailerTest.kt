package functional

import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import fr.dcproject.application.Env.TEST
import fr.dcproject.application.module
import fr.dcproject.common.email.Mailer
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.test.KoinTest
import org.koin.test.get

@KtorExperimentalLocationsAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("functional"), Tag("mail"))
class MailerTest : KoinTest {
    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    @Test
    @Tags(Tag("online"))
    fun `can be send an email`() {
        withTestApplication({ module(TEST) }) {
            get<Mailer>().sendEmail {
                Mail(
                    Email("passwordless-auth@dc-project.fr"),
                    "Test",
                    Email("fabrice.lecomte.be@gmail.com"),
                    Content("text/plain", "Email Work !")
                ).apply {
                    addContent(Content("text/html", "Email <b>Work</b> !"))
                }
            }
        }
    }
}
