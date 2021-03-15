package integration

import fr.dcproject.component.citizen.database.CitizenI.Name
import integration.steps.then.`And the response should contain`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.`when`.`When I send a PUT request`
import integration.steps.`when`.`with body`
import integration.steps.then.and
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have comment on article`
import integration.steps.given.`authenticated as`
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
                `with body`("""
                {
                  "content": "Hello mister"
                }
                """)
            } `Then the response should be` Created and {
                `And the response should not be null`()
                `And the response should contain`("$.target.id", "aa16c635-28da-46f0-9a89-934eef88c7ca")
                `And the response should contain`("$.content", "Hello mister")
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
            `When I send a GET request`("/articles/6166c078-ca97-4366-b0aa-2a5cd558c78a/comments") {
                `authenticated as`("Enrico", "Fermi")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.result[0].target.id", "6166c078-ca97-4366-b0aa-2a5cd558c78a")
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
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.currentPage", 1)
                `And the response should contain`("$.limit", 50)
                `And the response should contain`("$.result[0]createdBy.id", "292a20cc-4a60-489e-9866-a95d38ffaf47")
            }
        }
    }

    @Test
    fun `I can edit comment`() {
        withIntegrationApplication {
            `Given I have citizen`("Hubert", "Reeves")
            `Given I have article`(id = "bb05e4a3-55a1-4088-85e7-8d8c23be29b1")
            `Given I have comment on article`(article = "bb05e4a3-55a1-4088-85e7-8d8c23be29b1", createdBy = Name("Hubert", "Reeves"), id = "fd30d20f-656c-42c6-8955-f61c04537464")
            `When I send a PUT request`("/comments/fd30d20f-656c-42c6-8955-f61c04537464") {
                `authenticated as`("Hubert", "Reeves")
                `with body`("""
                Hello boy
                """)
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
                createdBy = Name("Alfred", "Kastler"),
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
