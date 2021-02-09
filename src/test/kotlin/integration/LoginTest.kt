package integration

import integration.prerequisite.CitizenPrerequisite
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"))
class LoginTest : BaseTest() {
    @Test
    fun `I can login`() {
        withIntegrationApplication {
            CitizenPrerequisite().createCitizen("Niels", "Bohr")
            handlePostRequest("/login") {
                """
                {
                  "username": "niels-bohr",
                  "password": "azerty"
                }
                """
            }.`should be respond` (HttpStatusCode.OK) {
                content
                    .`should not be null`()
                    .`should contain`("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.")
            }
        }
    }
}
