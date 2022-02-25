package integration

import fr.dcproject.component.citizen.database.CitizenI.Name
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have comment on constitution`
import integration.steps.given.`Given I have constitution`
import integration.steps.given.`authenticated as`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`And the response should contain list`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import integration.steps.`when`.Validate
import integration.steps.`when`.Validate.ALL
import integration.steps.`when`.Validate.REQUEST_BODY
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
@Tags(Tag("integration"), Tag("constitution"), Tag("comment"))
class `Comment constitutions routes` : BaseTest() {
    @Test
    fun `I can comment constitution`() {
        withIntegrationApplication {
            `Given I have citizen`("Nicolas", "Copernic")
            `Given I have constitution`(id = "1707c287-a472-4a62-89f2-9e85030e915c")
            `When I send a POST request`("/constitutions/1707c287-a472-4a62-89f2-9e85030e915c/comments") {
                `authenticated as`("Nicolas", "Copernic")
                `with body`(
                    """
                    {
                      "content": "Hello mister MARABOUTCHA"
                    }
                    """
                )
            } `Then the response should be` Created and {
                `And the response should not be null`()
                `And the response should contain`("$.target.id", "1707c287-a472-4a62-89f2-9e85030e915c")
                `And the response should contain`("$.content", "Hello mister MARABOUTCHA")
            }
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot comment constitution with bad request`() {
        withIntegrationApplication {
            `Given I have citizen`("Nicolas", "Copernic")
            `Given I have constitution`(id = "aa16c635-28da-46f0-9a89-934eef88c7ca")
            `When I send a POST request`("/constitutions/aa16c635-28da-46f0-9a89-934eef88c7ca/comments", ALL - REQUEST_BODY) {
                `authenticated as`("Nicolas", "Copernic")
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

    @Test
    fun `I can get all comment on constitution`() {
        withIntegrationApplication {
            `Given I have citizen`("Enrico", "Fermi")
            `Given I have constitution`(id = "6166c078-ca97-4366-b0aa-2a5cd558c78a")
            `Given I have comment on constitution`(constitution = "6166c078-ca97-4366-b0aa-2a5cd558c78a", createdBy = Name("Enrico", "Fermi"))
            `When I send a GET request`("/constitutions/6166c078-ca97-4366-b0aa-2a5cd558c78a/comments?page=1&limit=40&sort=votes") {
                `authenticated as`("Enrico", "Fermi")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.result[0].target.id", "6166c078-ca97-4366-b0aa-2a5cd558c78a")
            }
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot get all comment on constitution with wrong parameters`() {
        withIntegrationApplication {
            `Given I have citizen`("Enrico", "Fermi")
            `Given I have constitution`(id = "6166c078-ca97-4366-b0aa-2a5cd558c78a")
            `Given I have comment on constitution`(constitution = "6166c078-ca97-4366-b0aa-2a5cd558c78a", createdBy = Name("Enrico", "Fermi"))
            `When I send a GET request`("/constitutions/6166c078-ca97-4366-b0aa-2a5cd558c78a/comments?page=1&limit=40&sort=wrong", ALL - Validate.REQUEST_PARAM) {
                `authenticated as`("Enrico", "Fermi")
            } `Then the response should be` BadRequest and {
                `And the response should not be null`()
                `And the response should contain`("$.invalidParams[*].name", ".sort")
                `And the response should contain`("$.invalidParams[*].reason", "must be one of: 'votes', 'createdAt'")
            }
        }
    }

    @Test
    fun `I can get comments on constitutions of the current citizen`() {
        withIntegrationApplication {
            `Given I have citizen`("Charles", "Darwin", id = "46e0bda9-ca6a-4c65-a58b-7e7267a0bbc5")
            `Given I have constitution`(id = "34ddd50a-da00-4a90-a869-08baa2a121be", createdBy = Name("Charles", "Darwin"))
            `Given I have comment on constitution`(constitution = "34ddd50a-da00-4a90-a869-08baa2a121be", createdBy = Name("Charles", "Darwin"))
            `When I send a GET request`("/citizens/46e0bda9-ca6a-4c65-a58b-7e7267a0bbc5/comments/constitutions") {
                `authenticated as`("Charles", "Darwin")
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.currentPage", 1)
                `And the response should contain`("$.limit", 50)
                `And the response should contain`("$.result[0].createdBy.id", "46e0bda9-ca6a-4c65-a58b-7e7267a0bbc5")
                `And the response should contain`("$.result[0].target.id", "34ddd50a-da00-4a90-a869-08baa2a121be")
                `And the response should contain list`("$.result[*]", 1)
            }
        }
    }
}
