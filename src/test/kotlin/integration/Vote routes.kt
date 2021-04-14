package integration

import fr.dcproject.component.citizen.database.CitizenI.Name
import integration.steps.`when`.Validate
import integration.steps.`when`.Validate.ALL
import integration.steps.`when`.Validate.REQUEST_PARAM
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a PUT request`
import integration.steps.`when`.`with body`
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have comment on article`
import integration.steps.given.`Given I have constitution`
import integration.steps.given.`Given I have vote +1 on article`
import integration.steps.given.`Given I have vote -1 on article`
import integration.steps.given.`authenticated as`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("vote"))
class `Vote routes` : BaseTest() {
    @Test
    fun `I can vote article`() {
        withIntegrationApplication {
            `Given I have citizen`("Thalès", "Milet")
            `Given I have article`(id = "835c5101-ca39-4038-a4e6-da6ee62ca6d5")
            `When I send a PUT request`("/articles/835c5101-ca39-4038-a4e6-da6ee62ca6d5/vote") {
                `authenticated as`("Thalès", "Milet")
                `with body`(
                    """
                    {
                      "note": 1
                    }
                    """
                )
            } `Then the response should be` Created
        }
    }

    @Test
    fun `I can vote constitution`() {
        withIntegrationApplication {
            `Given I have citizen`("Gregor", "Mendel")
            `Given I have constitution`(id = "76e79c89-efc1-492d-9e8f-dc9717363a11")
            `When I send a PUT request`("/constitutions/76e79c89-efc1-492d-9e8f-dc9717363a11/vote") {
                `authenticated as`("Gregor", "Mendel")
                `with body`(
                    """
                    {
                      "note": 1
                    }
                    """
                )
            } `Then the response should be` Created
        }
    }

    @Test
    fun `I can get votes of current citizen`() {
        withIntegrationApplication {
            `Given I have citizen`("Carl", "Gauss", id = "c044823d-e778-4256-9016-b1334bf933d3")
            `Given I have article`("7c9286db-470d-448c-aab1-3f0b072213b1")
            `Given I have vote +1 on article`("7c9286db-470d-448c-aab1-3f0b072213b1", Name("Carl", "Gauss"))
            `When I send a GET request`("/citizens/c044823d-e778-4256-9016-b1334bf933d3/votes/articles?page=1&limit=50") {
                `authenticated as`("Carl", "Gauss")
            } `Then the response should be` OK and {
                `And the response should contain`("$.currentPage", 1)
                `And the response should contain`("$.limit", 50)
                `And the response should contain`("$.total", 1)
                `And the response should contain`("$.result[0].note", 1)
            }
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot get votes of current citizen with wrong request`() {
        withIntegrationApplication {
            `Given I have citizen`("Carl", "Gauss", id = "c044823d-e778-4256-9016-b1334bf933d3")
            `Given I have article`("7c9286db-470d-448c-aab1-3f0b072213b1")
            `Given I have vote +1 on article`("7c9286db-470d-448c-aab1-3f0b072213b1", Name("Carl", "Gauss"))
            `When I send a GET request`("/citizens/c044823d-e778-4256-9016-b1334bf933d3/votes/articles?page=1&limit=60", ALL - REQUEST_PARAM) {
                `authenticated as`("Carl", "Gauss")
            } `Then the response should be` HttpStatusCode.BadRequest and {
                `And the response should not be null`()
                `And the response should contain`("$.invalidParams[0].name", ".limit")
                `And the response should contain`("$.invalidParams[0].reason", "must be at most '50'")
            }
        }
    }

    @Test
    fun `I can get votes of current citizen by target ids`() {
        withIntegrationApplication {
            `Given I have citizen`("Rosalind", "Franklin", id = "ab3269f0-877b-46b2-ae1a-e7e7d1c12132")
            `Given I have article`("4d457f53-b937-4622-9542-d5f689d3716b")
            `Given I have vote +1 on article`("4d457f53-b937-4622-9542-d5f689d3716b", Name("Rosalind", "Franklin"))
            `Given I have article`("117ef3e6-a740-4d04-9a4a-a800a5f274b4")
            `Given I have vote -1 on article`("117ef3e6-a740-4d04-9a4a-a800a5f274b4", Name("Rosalind", "Franklin"))
            `When I send a GET request`("/citizens/ab3269f0-877b-46b2-ae1a-e7e7d1c12132/votes?id=4d457f53-b937-4622-9542-d5f689d3716b&id=117ef3e6-a740-4d04-9a4a-a800a5f274b4") {
                `authenticated as`("Rosalind", "Franklin")
            } `Then the response should be` OK and {
                `And the response should contain`("$[0].note", -1)
                `And the response should contain`("$[1].note", 1)
            }
        }
    }

    @Test
    fun `I can vote a comment`() {
        withIntegrationApplication {
            `Given I have citizen`("Antoine", "Lavoisier", id = "ab3269f0-877b-46b2-ae1a-e7e7d1c12132")
            `Given I have article`("54428366-e71e-4961-876c-8a13df5e4b41")
            `Given I have comment on article`(
                createdBy = Name("Antoine", "Lavoisier"),
                article = "54428366-e71e-4961-876c-8a13df5e4b41",
                id = "e793eccc-456b-4450-a292-46d592229b74",
            )
            `When I send a PUT request`("/comments/e793eccc-456b-4450-a292-46d592229b74/vote") {
                `authenticated as`("Antoine", "Lavoisier")
                `with body`(
                    """
                    {
                      "note": -1
                    }
                    """
                )
            } `Then the response should be` Created and {
                `And the response should contain`("$.down", 1)
            }
        }
    }
}
