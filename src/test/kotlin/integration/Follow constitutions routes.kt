package integration

import integration.steps.`And the response should be null`
import integration.steps.`And the response should contain`
import integration.steps.`And the response should not be null`
import integration.steps.`Then the response should be`
import integration.steps.`when`.`When I send a DELETE request`
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.and
import integration.steps.given.`And follow constitution`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have constitution`
import integration.steps.given.`authenticated as`
import integration.steps.given.`with no content`
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("constitution"), Tag("follow"))
class `Follow constitutions routes` : BaseTest() {
    @Test
    fun `I can follow constitution`() {
        withIntegrationApplication {
            `Given I have citizen`("Dmitri", "Mendeleïev")
            `Given I have constitution`(id = "f6553f5e-0cf5-476a-b84e-15e52ec9d8f9")
            `When I send a POST request`("/constitutions/f6553f5e-0cf5-476a-b84e-15e52ec9d8f9/follows") {
                `authenticated as`("Dmitri", "Mendeleïev")
                `with no content`()
            } `Then the response should be` Created
        }
    }

    @Test
    fun `I can get follow constitution`() {
        withIntegrationApplication {
            `Given I have constitution`(id = "7f642078-8e74-47fc-9712-0c37531674a0")
            `Given I have citizen`("André-Marie", "Ampère", id = "877b45b0-302d-487d-8944-6d03ccdbd0f8") {
                `And follow constitution`("7f642078-8e74-47fc-9712-0c37531674a0")
            }
            `When I send a GET request`("/citizens/877b45b0-302d-487d-8944-6d03ccdbd0f8/follows/constitutions") {
                `authenticated as`("André-Marie", "Ampère")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.current_page", 1)
                `And the response should contain`("$.limit", 50)
            }
        }
    }

    @Test
    fun `I can unfollow constitution`() {
        withIntegrationApplication {
            `Given I have constitution`(id = "4ea03776-a28c-4202-9b90-db1b942946c1")
            `Given I have citizen`("Claude", "Ptolémée") {
                `And follow constitution`("4ea03776-a28c-4202-9b90-db1b942946c1")
            }
            `When I send a DELETE request`("/constitutions/4ea03776-a28c-4202-9b90-db1b942946c1/follows") {
                `authenticated as`("Claude", "Ptolémée")
                `with no content`()
            } `Then the response should be` NoContent and {
                `And the response should be null`()
            }
        }
    }

    @Test
    fun `I can know if I follow an constitution`() {
        withIntegrationApplication {
            `Given I have constitution`(id = "5141e781-ebcd-4de0-8c2b-7d23d4cd58b5")
            `Given I have citizen`("Denis", "Papin") {
                `And follow constitution`("5141e781-ebcd-4de0-8c2b-7d23d4cd58b5")
            }
            `When I send a GET request`("/constitutions/5141e781-ebcd-4de0-8c2b-7d23d4cd58b5/follows") {
                `authenticated as`("Denis", "Papin")
                `with no content`()
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.target.id", "5141e781-ebcd-4de0-8c2b-7d23d4cd58b5")
            }
        }
    }
}
