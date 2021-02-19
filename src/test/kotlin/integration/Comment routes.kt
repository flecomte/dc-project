package integration

import integration.steps.`And the response should not be null`
import integration.steps.`Then the response should be`
import integration.steps.`when`.`When I send a GET request`
import integration.steps.and
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have comment on article`
import integration.steps.given.`authenticated as`
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
}
