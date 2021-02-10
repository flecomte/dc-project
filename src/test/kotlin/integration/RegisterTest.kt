package integration

import integration.asserts.`Then the response should be`
import integration.asserts.`when`.`When I send a POST request`
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not be null`
import org.junit.experimental.categories.Category
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("auth"))
class RegisterTest : BaseTest() {
    @Test
    @Category(RegisterTest::class)
    fun `I can register`() {
        withIntegrationApplication {
            `When I send a POST request`("/register") {
                """
                {
                  "name": {"first_name":"George", "last_name":"MICHEL"},
                  "birthday": "2001-01-01",
                  "user":{
                    "username": "george-junior",
                    "password": "azerty"
                  },
                  "email": "george-junior@gmail.com"
                }
                """
            }.`Then the response should be`(HttpStatusCode.OK) {
                content
                    .`should not be null`()
                    .`should contain`("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.")
            }
        }
    }

    @Test
    fun `I cannot register if no username was sent`() {
        withIntegrationApplication {
            `When I send a POST request`("/register") {
                """
                {
                  "name": {"first_name":"George2", "last_name":"MICHEL2"},
                  "birthday": "2001-01-01",
                  "user":{
                    "username": "",
                    "password": ""
                  }
                }
                """
            }.`Then the response should be`(HttpStatusCode.BadRequest) {
                content.`should be null`()
            }
        }
    }
}
