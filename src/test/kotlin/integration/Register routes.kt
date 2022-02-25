package integration

import integration.steps.then.`And the response should be null`
import integration.steps.then.`And the response should contain pattern`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import integration.steps.`when`.Validate
import integration.steps.`when`.`When I send a POST request`
import integration.steps.`when`.`with body`
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("auth"), Tag("register"))
class `Register routes` : BaseTest() {
    @Test
    fun `I can register`() {
        withIntegrationApplication {
            `When I send a POST request`("/register") {
                `with body`(
                    """
                    {
                      "name": {"firstName":"George", "lastName":"MICHEL"},
                      "birthday": "2001-01-01",
                      "user":{
                        "username": "george-junior",
                        "password": "Azerty123!"
                      },
                      "email": "george-junior@gmail.com"
                    }
                    """
                )
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain pattern`("$.token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.")
            }
        }
    }

    @Test
    fun `I cannot register if no username was sent`() {
        withIntegrationApplication {
            `When I send a POST request`("/register", Validate.ALL - Validate.REQUEST_BODY) {
                `with body`(
                    """
                    {
                      "name": {"firstName":"George2", "lastName":"MICHEL2"},
                      "birthday": "2001-01-01",
                      "user":{
                        "password": ""
                      },
                      "email": "george-junior@gmail.com"
                    }
                    """
                )
            } `Then the response should be` BadRequest and {
                `And the response should be null`()
            }
        }
    }
}
