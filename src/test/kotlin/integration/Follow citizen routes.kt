package integration

import integration.steps.`when`.`When I send a DELETE request`
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.given.`And follow citizen`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`authenticated as`
import integration.steps.given.`with no content`
import integration.steps.then.`And the response should be null`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("article"), Tag("follow"))
class `Follow citizen routes` : BaseTest() {
    @Test
    fun `I can follow citizen`() {
        withIntegrationApplication {
            /* Followed user */
            `Given I have citizen`("John", "Glenn", id = "7e1580c5-05b7-4557-84f4-faac9f0a9441")
            /* Current user */
            `Given I have citizen`("Valentina", "Terechkova")
            `When I send a POST request`("/citizens/7e1580c5-05b7-4557-84f4-faac9f0a9441/follows") {
                `authenticated as`("Valentina", "Terechkova")
                `with no content`()
            } `Then the response should be` Created
        }
    }

    @Test
    fun `I can get my follow citizen`() {
        withIntegrationApplication {
            /* Followed user */
            `Given I have citizen`("Jean-Loup", "Chrétien", id = "c2432b94-a509-4116-a8b6-9774bc963372")
            /* Current user */
            `Given I have citizen`("John", "Young", id = "6d41ce65-9df7-47e0-af46-8da4a909490b") {
                `And follow citizen`("c2432b94-a509-4116-a8b6-9774bc963372")
            }
            /* Get my all follows */
            `When I send a GET request`("/citizens/6d41ce65-9df7-47e0-af46-8da4a909490b/follows/citizens") {
                `authenticated as`("John", "Young")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.currentPage", 1)
                `And the response should contain`("$.limit", 50)
            }
        }
    }

    @Test
    fun `I can unfollow citizen`() {
        withIntegrationApplication {
            /* Followed user */
            `Given I have citizen`("Bruce", "McCandless", id = "680c7af7-d2de-4249-bfcb-47007ef546fe")
            /* Current user */
            `Given I have citizen`("Jean-François", "Clervoy", id = "a12455ae-1047-43ff-826d-0d826dbe90f7") {
                `And follow citizen`("680c7af7-d2de-4249-bfcb-47007ef546fe")
            }
            `When I send a DELETE request`("/citizens/680c7af7-d2de-4249-bfcb-47007ef546fe/follows") {
                `authenticated as`("Jean-François", "Clervoy")
                `with no content`()
            } `Then the response should be` NoContent and {
                `And the response should be null`()
            }
        }
    }

    @Test
    fun `I can know if I follow an citizen`() {
        withIntegrationApplication {
            /* Followed user */
            `Given I have citizen`("Eugene", "Cernan", id = "c755788f-7f48-4cde-8ff0-e75bcffdafc2")
            /* Current user */
            `Given I have citizen`("Buzz", "Aldrin", id = "39e2915a-e96f-43ea-babd-bd339d8bf197") {
                `And follow citizen`("c755788f-7f48-4cde-8ff0-e75bcffdafc2")
            }
            `When I send a GET request`("/citizens/c755788f-7f48-4cde-8ff0-e75bcffdafc2/follows") {
                `authenticated as`("Buzz", "Aldrin")
                `with no content`()
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.target.id", "c755788f-7f48-4cde-8ff0-e75bcffdafc2")
            }
        }
    }
}
