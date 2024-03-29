package integration

import integration.steps.given.`Given I have citizen`
import integration.steps.given.`authenticated as`
import integration.steps.then.`And have property`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import integration.steps.then.`which contains`
import integration.steps.`when`.Validate
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a PUT request`
import integration.steps.`when`.`with body`
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("citizen"))
class `Citizen routes` : BaseTest() {
    @Test
    fun `I can get Citizens information`() {
        withIntegrationApplication {
            `Given I have citizen`("Jean", "Perrin", id = "5267a5c6-af42-4a02-aa2b-6b71d2e43973")
            `When I send a GET request`("/citizens?page=1&limit=5&sort=createdAt") {
                `authenticated as`("Jean", "Perrin")
            } `Then the response should be` OK and {
                `And the response should not be null`()
            }
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot get Citizens information with wrong request`() {
        withIntegrationApplication {
            `Given I have citizen`("Jean", "Perrin", id = "5267a5c6-af42-4a02-aa2b-6b71d2e43973")
            `When I send a GET request`("/citizens?page=1&limit=5&sort=created_at", Validate.ALL - Validate.REQUEST_PARAM) {
                `authenticated as`("Jean", "Perrin")
            } `Then the response should be` BadRequest and {
                `And the response should not be null`()
            }
        }
    }

    @Test
    fun `I can get specific Citizen information`() {
        withIntegrationApplication {
            `Given I have citizen`("Linus", "Pauling", id = "47a05c0f-7329-46c3-a7d0-325db37e9114")
            `When I send a GET request`("/citizens/47a05c0f-7329-46c3-a7d0-325db37e9114") {
                `authenticated as`("Linus", "Pauling")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And have property`("$.id") `which contains` "47a05c0f-7329-46c3-a7d0-325db37e9114"
            }
        }
    }

    @Test
    fun `I can get my citizen information when I was connected`() {
        withIntegrationApplication {
            `Given I have citizen`("Henri", "Becquerel", id = "47356809-c8ef-4649-8b99-1c5cb9886d38")
            `When I send a GET request`("/citizens/current") {
                `authenticated as`("Henri", "Becquerel")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And have property`("$.id") `which contains` "47356809-c8ef-4649-8b99-1c5cb9886d38"
            }
        }
    }

    @Test
    fun `I can change my password`() {
        withIntegrationApplication {
            `Given I have citizen`("Georges", "Charpak", id = "0c966522-4071-43e5-a3ca-cfff2557f2cf")
            `When I send a PUT request`("/citizens/0c966522-4071-43e5-a3ca-cfff2557f2cf/password/change") {
                `authenticated as`("Georges", "Charpak")
                `with body`(
                    """
                    {
                      "oldPassword": "Azerty123!",
                      "newPassword": "Qwerty123!"
                    }
                    """
                )
            } `Then the response should be` Created
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot change my password if request is bad formatted`() {
        withIntegrationApplication {
            `Given I have citizen`("Louis", "Breguet", id = "6cf2a19d-d15d-4ee5-b2a9-907afd26b525")
            `When I send a PUT request`("/citizens/6cf2a19d-d15d-4ee5-b2a9-907afd26b525/password/change", Validate.ALL - Validate.REQUEST_BODY) {
                `authenticated as`("Louis", "Breguet")
                `with body`(
                    """
                    {
                      "plup": "azerty",
                      "gloup": "qwerty"
                    }
                    """
                )
            } `Then the response should be` BadRequest
        }
    }
}
