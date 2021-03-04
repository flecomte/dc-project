package integration

import fr.dcproject.component.citizen.database.CitizenI.Name
import integration.steps.then.`And the response should contain list`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`Then the response should be`
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a PUT request`
import integration.steps.then.and
import integration.steps.given.`Given I have an opinion choice`
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have opinion on article`
import integration.steps.given.`authenticated as`
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("article"), Tag("opinion"))
class `Opinion routes` : BaseTest() {
    @Test
    fun `I can get all opinion choices`() {
        withIntegrationApplication {
            `Given I have an opinion choice`("Opinion1")
            `Given I have an opinion choice`("Opinion2")
            `When I send a GET request`("/opinions") {
            } `Then the response should be` OK and {
                `And the response should contain`("$.[0]name", "Opinion1")
                `And the response should contain`("$.[1]name", "Opinion2")
            }
        }
    }

    @Test
    fun `I can get one opinion choice`() {
        withIntegrationApplication {
            `Given I have an opinion choice`("Opinion3", id = "347ec243-0e76-4ab5-9884-7bd503cf5ab5")
            `When I send a GET request`("/opinions/347ec243-0e76-4ab5-9884-7bd503cf5ab5") {
            } `Then the response should be` OK and {
                `And the response should contain`("$.name", "Opinion3")
            }
        }
    }

    @Test
    fun `I can create opinion on article`() {
        withIntegrationApplication {
            `Given I have citizen`("Isaac", "Newton", id = "2f414045-95d9-42ca-a3a9-8cdde52ad253")
            `Given I have an opinion choice`("Opinion4", id = "0f4f1721-3136-44f1-9f31-1459f3317b15")
            `Given I have article`(id = "9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b", createdBy = Name("Isaac", "Newton"))
            `When I send a PUT request`("/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/opinions") {
                `authenticated as`("Isaac", "Newton")
                """
                {
                  "ids": [
                    "0f4f1721-3136-44f1-9f31-1459f3317b15"
                  ]
                }
                """
            } `Then the response should be` Created
        }
    }

    @Test
    fun `I can get all opinions of citizen filtered by target ids`() {
        withIntegrationApplication {
            `Given I have article`("4eb8a2c6-ba63-4c1e-919f-72017132a54e")
            `Given I have citizen`("Albert", "Jacquard", id = "c6392fc4-56f5-461b-8015-953a4da3029f")
            `Given I have an opinion choice`("Opinion5", id = "74d6e105-9aa6-4589-8776-82ce260bb6f4")
            `Given I have opinion on article`(
                "Opinion5",
                article = "4eb8a2c6-ba63-4c1e-919f-72017132a54e",
                Name("Albert", "Jacquard"),
                id = "994660e7-e9f4-4ae9-9290-b34d78663c7a"
            )
            `When I send a GET request`("/citizens/c6392fc4-56f5-461b-8015-953a4da3029f/opinions?id=4eb8a2c6-ba63-4c1e-919f-72017132a54e") {
                `authenticated as`("Albert", "Jacquard")
            } `Then the response should be` OK and {
                `And the response should contain`("$[0].name", "Opinion5")
            }
        }
    }

    @Test
    fun `I can receive opinion aggregation with article`() {
        withIntegrationApplication {
            `Given I have an opinion choice`("Opinion6")
            `Given I have an opinion choice`("Opinion7")
            `Given I have an opinion choice`("Opinion8")
            `Given I have citizen`("James", "Watt")
            `Given I have citizen`("Paul", "Langevin")
            `Given I have article`("bda8940a-6792-4f2b-936a-ba5c805c8487")
            `Given I have opinion on article`(
                "Opinion6",
                article = "bda8940a-6792-4f2b-936a-ba5c805c8487",
                Name("James", "Watt")
            )
            `Given I have opinion on article`(
                "Opinion7",
                article = "bda8940a-6792-4f2b-936a-ba5c805c8487",
                Name("James", "Watt")
            )
            `Given I have opinion on article`(
                "Opinion7",
                article = "bda8940a-6792-4f2b-936a-ba5c805c8487",
                Name("Paul", "Langevin")
            )
            `When I send a GET request`("/articles/bda8940a-6792-4f2b-936a-ba5c805c8487") `Then the response should be` OK and {
                `And the response should contain`("$.opinions.Opinion6", 1)
                `And the response should contain`("$.opinions.Opinion7", 2)
            }
        }
    }

    @Test
    fun `I can get all opinion of one citizen`() {
        withIntegrationApplication {
            `Given I have citizen`("Albert", "Einstein", id = "c1542096-3431-432d-8e35-9dc071d4c818")
            `Given I have an opinion choice`("Opinion9")
            `Given I have article`("8651b530-ac1b-4214-a784-706781371074")
            `Given I have opinion on article`(
                "Opinion9",
                article = "8651b530-ac1b-4214-a784-706781371074",
                Name("Albert", "Einstein")
            )
            `When I send a GET request`("/citizens/c1542096-3431-432d-8e35-9dc071d4c818/opinions/articles") {
                `authenticated as`("Albert", "Einstein")
            } `Then the response should be` OK and {
                `And the response should contain`("$.result[0].name", "Opinion9")
                `And the response should contain list`("$.result[*]", 1, 1)
            }
        }
    }
}
