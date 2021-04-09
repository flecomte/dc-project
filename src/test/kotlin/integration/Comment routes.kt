package integration

import fr.dcproject.component.citizen.database.CitizenI
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a PUT request`
import integration.steps.`when`.`with body`
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have comment on article`
import integration.steps.given.`authenticated as`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("comment"))
class `Comment routes` : BaseTest() {
    @Test
    fun `I can get comments children`() {
        withIntegrationApplication {
            `Given I have citizen`("John", "Dalton")
            `Given I have article`(id = "4c948e8f-eada-4e10-8d7d-7192affe1313")
            `Given I have comment on article`(id = "da22fc55-b0fd-42a5-a317-9583b1bb93c5", article = "4c948e8f-eada-4e10-8d7d-7192affe1313")
            `When I send a GET request`("/comments/da22fc55-b0fd-42a5-a317-9583b1bb93c5/children") {
                `authenticated as`("John", "Dalton")
            } `Then the response should be` OK and {
                `And the response should not be null`()
            }
        }
    }

    @Test
    fun `I can edit comment`() {
        withIntegrationApplication {
            `Given I have citizen`("Hubert", "Reeves")
            `Given I have article`(id = "bb05e4a3-55a1-4088-85e7-8d8c23be29b1")
            `Given I have comment on article`(article = "bb05e4a3-55a1-4088-85e7-8d8c23be29b1", createdBy = CitizenI.Name(
                "Hubert",
                "Reeves"
            ), id = "fd30d20f-656c-42c6-8955-f61c04537464")
            `When I send a PUT request`("/comments/fd30d20f-656c-42c6-8955-f61c04537464") {
                `authenticated as`("Hubert", "Reeves")
                `with body`(
                    """
                    {
                      "content": "Hello boy"
                    }
                    """
                )
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.content", "Hello boy")
            }
        }
    }

    @Test
    fun `I can get comment by its ID`() {
        withIntegrationApplication {
            `Given I have citizen`("Alfred", "Kastler")
            `Given I have article`(id = "3897465b-19d2-43a0-86ea-1e29dbb11ec9")
            `Given I have comment on article`(
                article = "3897465b-19d2-43a0-86ea-1e29dbb11ec9",
                createdBy = CitizenI.Name("Alfred", "Kastler"),
                id = "edd296a8-fc7a-4717-a2bb-9f035ceca3c2",
                content = "Hello boy"
            )
            `When I send a GET request`("/comments/edd296a8-fc7a-4717-a2bb-9f035ceca3c2") {
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.content", "Hello boy")
            }
        }
    }
}
