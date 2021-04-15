package integration

import fr.dcproject.component.citizen.database.CitizenI.Name
import integration.steps.`when`.`When I send a DELETE request`
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.`when`.`When I send a PUT request`
import integration.steps.`when`.`with body`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have workgroup`
import integration.steps.given.`With members`
import integration.steps.given.`authenticated as`
import integration.steps.given.`with no content`
import integration.steps.then.`And have property`
import integration.steps.then.`And the response should be null`
import integration.steps.then.`And the response should contain list`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("workgroup"))
class `Workgroup routes` : BaseTest() {
    @Test
    fun `I can get one workgroup`() {
        withIntegrationApplication {
            `Given I have citizen`("Stephen", "Hawking")
            `Given I have citizen`("Sadi", "Carnot", id = "be3b0926-8628-4426-804a-75188a6eb315")
            `Given I have citizen`("Joseph", "Fourier", id = "d9671eca-abaf-4b67-9230-3ece700c1ddb")
            `Given I have workgroup`(
                id = "ab469134-bf14-4856-b093-ae1aa990f977",
                name = "Les Mousquets",
                createdBy = Name("Stephen", "Hawking")
            ) {
                `With members`(
                    Name("Sadi", "Carnot"),
                    Name("Joseph", "Fourier"),
                )
            }
            `When I send a GET request`("/workgroups/ab469134-bf14-4856-b093-ae1aa990f977") {
                `authenticated as`("Stephen", "Hawking")
            } `Then the response should be` OK and {
                `And the response should contain`("$.id", "ab469134-bf14-4856-b093-ae1aa990f977")
                `And the response should contain`("$.name", "Les Mousquets")
                `And the response should contain`(
                    "$.members[*].citizen.name[?(@.firstName=='Stephen')].firstName",
                    "Stephen"
                )
                `And the response should contain`(
                    "$.members[*].citizen.name[?(@.firstName=='Sadi')].firstName",
                    "Sadi"
                )
            }
        }
    }

    @Test
    fun `I can create a workgroup`() {
        withIntegrationApplication {
            `Given I have citizen`("Werner", "Heisenberg")
            `When I send a POST request`("/workgroups") {
                `authenticated as`("Werner", "Heisenberg")
                `with body`(
                    """
                    {
                        "id":"f496d86d-6654-4068-91ff-90e1dbcc5f38",
                        "name":"Les Bouffons",
                        "description":"La vie est belle",
                        "anonymous":false
                    }
                    """
                )
            } `Then the response should be` Created and {
                `And the response should contain`("$.id", "f496d86d-6654-4068-91ff-90e1dbcc5f38")
                `And the response should contain`("$.name", "Les Bouffons")
                `And the response should contain`("$.description", "La vie est belle")
                `And the response should contain`("$.anonymous", false)
            }

            `When I send a GET request`("/workgroups/f496d86d-6654-4068-91ff-90e1dbcc5f38") {
                `authenticated as`("Werner", "Heisenberg")
            } `Then the response should be` OK
        }
    }

    @Test
    fun `I can edit a workgroup`() {
        withIntegrationApplication {
            `Given I have citizen`("John", "Wheeler")
            `Given I have citizen`("Heinrich", "Hertz", id = "94f92424-c257-4582-907c-98564a8c4ac9")
            `Given I have citizen`("William", "Thomson", id = "87909ba3-2069-431c-9924-219fd8411cf2")
            `Given I have workgroup`("aa875a24-0050-4252-9130-d37391714e26", createdBy = Name("John", "Wheeler")) {
                `With members`(
                    Name("Heinrich", "Hertz"),
                    Name("William", "Thomson"),
                )
            }
            `When I send a PUT request`("/workgroups/aa875a24-0050-4252-9130-d37391714e26") {
                `authenticated as`("John", "Wheeler")
                `with body`(
                    """
                    {
                        "name":"La ratatouille",
                        "description":"Une petite souris"
                    }
                    """
                )
            } `Then the response should be` OK and {
                `And the response should contain`("$.id", "aa875a24-0050-4252-9130-d37391714e26")
                `And the response should contain`("$.name", "La ratatouille")
                `And the response should contain`("$.description", "Une petite souris")

                `And have property`("$.members")
                `And the response should contain list`("$.members", 3)
                `And the response should contain`("$.members.[1]citizen.id", "94f92424-c257-4582-907c-98564a8c4ac9")
                `And the response should contain`("$.members.[2]citizen.id", "87909ba3-2069-431c-9924-219fd8411cf2")
            }

            `When I send a GET request`("/workgroups/aa875a24-0050-4252-9130-d37391714e26") {
                `authenticated as`("John", "Wheeler")
            } `Then the response should be` OK and {
                `And the response should contain`("$.id", "aa875a24-0050-4252-9130-d37391714e26")
                `And the response should contain`("$.name", "La ratatouille")
                `And the response should contain`("$.description", "Une petite souris")
            }
        }
    }

    @Test
    fun `I can delete a workgroup`() {
        withIntegrationApplication {
            `Given I have citizen`("Louis", "Néel")
            `Given I have workgroup`("ab469134-bf14-4856-b093-ae1aa990f977", createdBy = Name("Louis", "Néel"))
            `When I send a DELETE request`("/workgroups/ab469134-bf14-4856-b093-ae1aa990f977") {
                `authenticated as`("Louis", "Néel")
                `with no content`()
            } `Then the response should be` NoContent and {
                `And the response should be null`()
            }

            `When I send a GET request`("/workgroups/ab469134-bf14-4856-b093-ae1aa990f977") {
                `authenticated as`("Louis", "Néel")
            } `Then the response should be` NotFound
        }
    }

    @Test
    fun `I can get workgroups list`() {
        withIntegrationApplication {
            `Given I have citizen`("Max", "Planck")
            `Given I have workgroup`("3fd8edb6-c4b4-4c94-bc75-ddd9b290d32c")
            `When I send a GET request`("/workgroups") {
                `authenticated as`("Max", "Planck")
                `with no content`()
            } `Then the response should be` OK and {
                `And the response should contain`("$.result[0].id", "3fd8edb6-c4b4-4c94-bc75-ddd9b290d32c")
            }
        }
    }
}
