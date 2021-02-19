package integration

import integration.steps.`And the response should be null`
import integration.steps.`And the response should contain`
import integration.steps.`And the response should not be null`
import integration.steps.`Then the response should be`
import integration.steps.`when`.`When I send a DELETE request`
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.and
import integration.steps.given.`And follow article`
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have citizen`
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
@Tags(Tag("integration"), Tag("article"), Tag("follow"))
class `Follow articles routes` : BaseTest() {
    @Test
    fun `I can follow article`() {
        withIntegrationApplication {
            `Given I have citizen`("Louis", "Pasteur")
            `Given I have article`(id = "04754b7b-edef-4adc-af81-75e3aadeebea")
            `When I send a POST request`("/articles/04754b7b-edef-4adc-af81-75e3aadeebea/follows") {
                `authenticated as`("Louis", "Pasteur")
                `with no content`()
            } `Then the response should be` Created
        }
    }

    @Test
    fun `I can get follow article`() {
        withIntegrationApplication {
            `Given I have article`(id = "d743619a-1f6b-4d20-a2d6-8e81f0e6a4c8")
            `Given I have citizen`("Johannes", "Kepler", id = "e4592d53-3660-4264-8353-ebdbf5d9c41c") {
                `And follow article`("d743619a-1f6b-4d20-a2d6-8e81f0e6a4c8")
            }
            `When I send a GET request`("/citizens/e4592d53-3660-4264-8353-ebdbf5d9c41c/follows/articles") {
                `authenticated as`("Johannes", "Kepler")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.current_page", 1)
                `And the response should contain`("$.limit", 50)
            }
        }
    }

    @Test
    fun `I can unfollow article`() {
        withIntegrationApplication {
            `Given I have article`(id = "aad3aa9d-95fd-4919-9e84-46255f620e31")
            `Given I have citizen`("Thomas", "Edison") {
                `And follow article`("aad3aa9d-95fd-4919-9e84-46255f620e31")
            }
            `When I send a DELETE request`("/articles/aad3aa9d-95fd-4919-9e84-46255f620e31/follows") {
                `authenticated as`("Thomas", "Edison")
                `with no content`()
            } `Then the response should be` NoContent and {
                `And the response should be null`()
            }
        }
    }

    @Test
    fun `I can know if I follow an article`() {
        withIntegrationApplication {
            `Given I have article`(id = "3ee4e6d0-f312-4940-872d-1f578c8d824c")
            `Given I have citizen`("Marie", "Curie") {
                `And follow article`("3ee4e6d0-f312-4940-872d-1f578c8d824c")
            }
            `When I send a GET request`("/articles/3ee4e6d0-f312-4940-872d-1f578c8d824c/follows") {
                `authenticated as`("Marie", "Curie")
                `with no content`()
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.target.id", "3ee4e6d0-f312-4940-872d-1f578c8d824c")
            }
        }
    }
}
