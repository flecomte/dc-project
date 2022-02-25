package integration

import integration.steps.given.`Given I have citizen`
import integration.steps.given.`authenticated as`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import integration.steps.then.`and should contains`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.`when`.`with body`
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("auth"), Tag("login"))
class `Login routes` : BaseTest() {
    @Test
    fun `I can login with username and password`() {
        withIntegrationApplication {
            `Given I have citizen`("Niels", "Bohr")
            `When I send a POST request`("/login") {
                `with body`(
                    """
                    {
                      "username": "niels-bohr",
                      "password": "Azerty123!"
                    }
                    """
                )
            } `Then the response should be` OK and {
                `And the response should not be null`() `and should contains` "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9."
                // TODO valid requestBody
            }
        }
    }

    @Test
    fun `I can be connect with Passwordless auth`() {
        withIntegrationApplication {
            `Given I have citizen`("Leonhard", "Euler", "fabrice.lecomte.be@gmail.com", id = "c606110c-ff0e-4d09-a79e-74632d7bf7bd")
            `When I send a POST request`("/auth/passwordless") {
                `authenticated as`("Leonhard", "Euler")
                `with body`(
                    """
                    {
                      "url": "https://dc-project.fr/password/reset",
                      "email": "fabrice.lecomte.be@gmail.com"
                    }
                    """
                )
            } `Then the response should be` NoContent
        }
    }
}
