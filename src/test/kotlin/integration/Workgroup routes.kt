package integration

import fr.dcproject.component.citizen.CitizenI.Name
import integration.steps.`And the response should be null`
import integration.steps.`And the response should contain list`
import integration.steps.`And the response should contain`
import integration.steps.`Then the response should be`
import integration.steps.`when`.`When I send a DELETE request`
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.`when`.`When I send a PUT request`
import integration.steps.and
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have workgroup`
import integration.steps.given.`With members`
import integration.steps.given.`authenticated as`
import integration.steps.given.`with no content`
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
                    "$.members[*].citizen.name[?(@.first_name=='Stephen')].first_name",
                    "Stephen"
                )
                `And the response should contain`(
                    "$.members[*].citizen.name[?(@.first_name=='Sadi')].first_name",
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
                """
                {
                    "id":"f496d86d-6654-4068-91ff-90e1dbcc5f38",
                    "name":"Les Bouffons",
                    "description":"La vie est belle",
                    "anonymous":false
                }
                """
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

    @Test
    fun `I can add member to workgroup`() {
        withIntegrationApplication {
            `Given I have citizen`("Blaise", "Pascal")
            `Given I have citizen`("Roger", "Penrose", id = "6d883fe7-5fc0-4a50-8858-72230673eba4")
            `Given I have citizen`("Alessandro", "Volta", id = "b5bac515-45d4-4aeb-9b6d-2627a0bbc419")
            `Given I have workgroup`("b0ea1922-3bc6-44e2-aa7c-40158998cfbb", createdBy = Name("Blaise", "Pascal"))
            `When I send a POST request`("/workgroups/b0ea1922-3bc6-44e2-aa7c-40158998cfbb/members") {
                `authenticated as`("Blaise", "Pascal")
                """
                [
                    {
                        "citizen": {"id":"6d883fe7-5fc0-4a50-8858-72230673eba4"},
                        "roles": ["MASTER"]
                    },
                    {
                        "citizen": {"id":"b5bac515-45d4-4aeb-9b6d-2627a0bbc419"},
                        "roles": ["MASTER"]
                    }
                ]
                """
            } `Then the response should be` Created
        }
    }

    @Test
    fun `I can remove member to workgroup`() {
        withIntegrationApplication {
            `Given I have citizen`("Heinrich", "Hertz", id = "94f92424-c257-4582-907c-98564a8c4ac9")
            `Given I have citizen`("William", "Thomson", id = "87909ba3-2069-431c-9924-219fd8411cf2")
            `Given I have citizen`("Paul", "Dirac", id = "1baf48bb-02bc-4d8f-ac86-33335354f5e7")
            `Given I have workgroup`("b6c975df-dd44-4e99-adc1-f605746b0e11", createdBy = Name("Heinrich", "Hertz")) {
                `With members`(
                    Name("William", "Thomson"),
                    Name("Paul", "Dirac"),
                )
            }
            `When I send a DELETE request`("/workgroups/b6c975df-dd44-4e99-adc1-f605746b0e11/members") {
                `authenticated as`("Heinrich", "Hertz")
                """
                [
                  {
                    "citizen": {"id":"87909ba3-2069-431c-9924-219fd8411cf2"},
                    "roles": ["MASTER"]
                  }
                ]
                """
            } `Then the response should be` OK and {
                `And the response should contain list`("$", 2, 2)
                `And the response should contain`("$.[0]citizen.id", "94f92424-c257-4582-907c-98564a8c4ac9")
                `And the response should contain`("$.[1]citizen.id", "1baf48bb-02bc-4d8f-ac86-33335354f5e7")
            }
        }
    }

    @Test
    fun `I can update members on workgroup`() {
        withIntegrationApplication {
            `Given I have citizen`("Leon", "Foucault")
            `Given I have citizen`("Sadi", "Carnot", id = "be3b0926-8628-4426-804a-75188a6eb315")
            `Given I have citizen`("Joseph", "Fourier", id = "b49e20c1-8393-45d6-a6a0-3fa5c71cbdc1")
            `Given I have citizen`("Georg", "Ohm")
            `Given I have workgroup`("784fe6bc-7635-4ae2-b080-3a4743b998bf", createdBy = Name("Leon", "Foucault")) {
                `With members`(
                    Name("Sadi", "Carnot"),
                    Name("Joseph", "Fourier"),
                )
            }
            `When I send a PUT request`("/workgroups/784fe6bc-7635-4ae2-b080-3a4743b998bf/members") {
                `authenticated as`("Leon", "Foucault")
                """
                [
                  {
                    "citizen": {"id":"be3b0926-8628-4426-804a-75188a6eb315"},
                    "roles": ["MASTER"]
                  },
                  {
                    "citizen": {"id":"b49e20c1-8393-45d6-a6a0-3fa5c71cbdc1"},
                    "roles": ["MASTER"]
                  }
                ]
                """
            } `Then the response should be` OK and {
                `And the response should contain list`("$", 2, 2)
                `And the response should contain`("$.[0]citizen.id", "be3b0926-8628-4426-804a-75188a6eb315")
                `And the response should contain`("$.[1]citizen.id", "b49e20c1-8393-45d6-a6a0-3fa5c71cbdc1")
            }
        }
    }
}
