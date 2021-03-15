package integration

import fr.dcproject.component.citizen.database.CitizenI.Name
import integration.steps.then.`And the response should contain list`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.`when`.`with body`
import integration.steps.then.and
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have comment on constitution`
import integration.steps.given.`Given I have constitution`
import integration.steps.given.`authenticated as`
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.setBody
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
                `with body`("""
                {
                  "content": "Hello mister"
                }
                """)
            } `Then the response should be` Created and {
                `And the response should not be null`()
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
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain`("$.current_page", 1)
                `And the response should contain`("$.limit", 50)
                `And the response should contain`("$.result[0].created_by.id", "46e0bda9-ca6a-4c65-a58b-7e7267a0bbc5")
                `And the response should contain`("$.result[0].target.id", "34ddd50a-da00-4a90-a869-08baa2a121be")
                `And the response should contain list`("$.result[*]", 1, 1)
            }
        }
    }
}
