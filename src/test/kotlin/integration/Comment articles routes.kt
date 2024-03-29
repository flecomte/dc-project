package integration

import fr.dcproject.component.citizen.database.CitizenI.Name
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have comment on article`
import integration.steps.given.`authenticated as`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import integration.steps.`when`.Validate.ALL
import integration.steps.`when`.Validate.REQUEST_BODY
import integration.steps.`when`.Validate.REQUEST_PARAM
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.`when`.`with body`
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("comment"), Tag("article"))
class `Comment articles routes` : BaseTest() {
    @Test
    fun `I can comment article`() {
        withIntegrationApplication {
            `Given I have citizen`("Michael", "Faraday")
            `Given I have article`(id = "aa16c635-28da-46f0-9a89-934eef88c7ca")
            `When I send a POST request`("/articles/aa16c635-28da-46f0-9a89-934eef88c7ca/comments") {
                `authenticated as`("Michael", "Faraday")
                `with body`(
                    """
                    {
                      "content": "Hello mister MARABOUTCHA"
                    }
                    """
                )
            } `Then the response should be` Created and {
                `And the response should not be null`()
                `And the response should contain`("$.target.id", "aa16c635-28da-46f0-9a89-934eef88c7ca")
                `And the response should contain`("$.content", "Hello mister MARABOUTCHA")
            }
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot comment article with bad request`() {
        withIntegrationApplication {
            `Given I have citizen`("Michael", "Faraday")
            `Given I have article`(id = "aa16c635-28da-46f0-9a89-934eef88c7ca")
            `When I send a POST request`("/articles/aa16c635-28da-46f0-9a89-934eef88c7ca/comments", ALL - REQUEST_BODY) {
                `authenticated as`("Michael", "Faraday")
                `with body`(
                    """
                    {
                      "content": "To small content"
                    }
                    """
                )
            } `Then the response should be` BadRequest and {
                `And the response should not be null`()
                `And the response should contain`("$.invalidParams[0].name", ".content")
                `And the response should contain`("$.invalidParams[0].reason", "must have at least 20 characters")
            }
        }
    }

    /* TODO add more comment on article */
    @Test
    fun `I can get all comment on article`() {
        withIntegrationApplication {
            `Given I have citizen`("Enrico", "Fermi")
            `Given I have article`(id = "6166c078-ca97-4366-b0aa-2a5cd558c78a")
            `Given I have comment on article`(article = "6166c078-ca97-4366-b0aa-2a5cd558c78a", createdBy = Name("Enrico", "Fermi"))
            `When I send a GET request`("/articles/6166c078-ca97-4366-b0aa-2a5cd558c78a/comments?page=1&limit=40&sort=votes") {
                `authenticated as`("Enrico", "Fermi")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.result[0].target.id", "6166c078-ca97-4366-b0aa-2a5cd558c78a")
            }
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot get all comment on article with wrong parameters`() {
        withIntegrationApplication {
            `Given I have citizen`("Enrico", "Fermi")
            `Given I have article`(id = "6166c078-ca97-4366-b0aa-2a5cd558c78a")
            `Given I have comment on article`(article = "6166c078-ca97-4366-b0aa-2a5cd558c78a", createdBy = Name("Enrico", "Fermi"))
            `When I send a GET request`("/articles/6166c078-ca97-4366-b0aa-2a5cd558c78a/comments?page=1&limit=40&sort=wrong", ALL - REQUEST_PARAM) {
                `authenticated as`("Enrico", "Fermi")
            } `Then the response should be` BadRequest and {
                `And the response should not be null`()
                `And the response should contain`("$.invalidParams[*].name", ".sort")
                `And the response should contain`("$.invalidParams[*].reason", "must be one of: 'votes', 'createdAt'")
            }
        }
    }

    /* TODO add votes */
    @Test
    fun `I can get all comment on article sorted by votes`() {
        withIntegrationApplication {
            `Given I have citizen`("Pierre", "Curie")
            `Given I have article`(id = "5e209f63-57ce-43ca-922a-273b0d62f567")
            `Given I have comment on article`(article = "5e209f63-57ce-43ca-922a-273b0d62f567", createdBy = Name("Pierre", "Curie"))
            `When I send a GET request`("/articles/5e209f63-57ce-43ca-922a-273b0d62f567/comments?sort=votes") {
                `authenticated as`("Pierre", "Curie")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.result[0].votes.up", 0)
            }
        }
    }

    @Test
    fun `I can get comments on articles of the current citizen`() {
        withIntegrationApplication {
            `Given I have citizen`("Erwin", "Schrodinger", id = "292a20cc-4a60-489e-9866-a95d38ffaf47")
            `Given I have article`(id = "17df7fb9-b388-4e20-ab19-29c29972da01", createdBy = Name("Erwin", "Schrodinger"))
            `Given I have comment on article`(article = "17df7fb9-b388-4e20-ab19-29c29972da01", createdBy = Name("Erwin", "Schrodinger"))
            `When I send a GET request`("/citizens/292a20cc-4a60-489e-9866-a95d38ffaf47/comments/articles") {
                `authenticated as`("Erwin", "Schrodinger")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.currentPage", 1)
                `And the response should contain`("$.limit", 50)
                `And the response should contain`("$.result[0]createdBy.id", "292a20cc-4a60-489e-9866-a95d38ffaf47")
            }
        }
    }
}
