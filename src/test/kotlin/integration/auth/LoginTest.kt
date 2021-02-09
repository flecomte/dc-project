package integration.auth

import integration.BaseTest
import integration.`Then the response should be`
import integration.prerequisite.`Given I have citizen`
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
@Tags(Tag("integration"), Tag("auth"))
class LoginTest : BaseTest() {
    @Test
    fun `I can login with username and password`() {
        withIntegrationApplication {
            `Given I have citizen`("Niels", "Bohr")
            `I send a POST request`("/login") {
                """
                {
                  "username": "niels-bohr",
                  "password": "azerty"
                }
                """
            }.`Then the response should be` (HttpStatusCode.OK) {
                content
                    .`should not be null`()
                    .`should contain`("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.")
            }
        }
    }
}
