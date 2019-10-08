import fr.dcproject.Env
import fr.dcproject.messages.Mailer
import fr.dcproject.module
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.get

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MailerTest: KoinTest, AutoCloseKoinTest() {
    @Test
    fun `can be send an email`() {
        withTestApplication({ module(Env.TEST) }) {
            get<Mailer>().sendEmail(
                "reset-password@dc-project.fr",
                "fabrice.lecomte.be@gmail.com",
                "Email Work !",
                "Test"
            )
        }
    }
}