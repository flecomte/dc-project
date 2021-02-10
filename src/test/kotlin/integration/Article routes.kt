package integration

import integration.asserts.`And have property`
import integration.asserts.`And the response should not be null`
import integration.asserts.`Then the response should be`
import integration.asserts.`when`.`When I send a GET request`
import integration.asserts.`when`.`When I send a POST request`
import integration.asserts.`whish contains`
import integration.asserts.and
import integration.asserts.given.`Given I have article created by workgroup`
import integration.asserts.given.`Given I have article`
import integration.asserts.given.`Given I have articles`
import integration.asserts.given.`Given I have citizen`
import integration.asserts.given.`Given I have workgroup`
import integration.asserts.given.`authenticated as`
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("citizen"))
class `Article routes` : BaseTest() {
    @Test
    fun `I can get article list`() {
        withIntegrationApplication {
            `Given I have articles`(3)
            `When I send a GET request`("/articles") `Then the response should be` OK and {
                `And the response should not be null`
            }
        }
    }

    @Test
    fun `I can get articles filtered by workgroup`() {
        withIntegrationApplication {
            `Given I have articles`(3)
            `Given I have workgroup`(name = "Les papy", id = "2bccd5a7-9082-4b31-88f8-e25d70b22b12")
            `Given I have article created by workgroup`("2bccd5a7-9082-4b31-88f8-e25d70b22b12")
            `When I send a GET request`("/articles?workgroup=2bccd5a7-9082-4b31-88f8-e25d70b22b12") `Then the response should be` OK and {
                `And the response should not be null`
                `And have property`("$.total") `whish contains` 1
                `And have property`("$.result[0]workgroup.name") `whish contains` "Les papy"
            }
        }
    }

    @Test
    fun `I can get versions of article by the id`() {
        withIntegrationApplication {
            `Given I have article`(id = "13e6091c-8fed-4600-b079-a97a6b7a9800")
            `When I send a GET request`("/articles/13e6091c-8fed-4600-b079-a97a6b7a9800/versions") `Then the response should be` OK and {
                `And the response should not be null`
                `And have property`("$.total") `whish contains` 1
                `And have property`("$.result[0].id") `whish contains` "13e6091c-8fed-4600-b079-a97a6b7a9800"
            }
        }
    }

    @Test
    fun `I can get article by id`() {
        withIntegrationApplication {
            `Given I have article`(id = "65cda9f3-8991-4420-8d41-1da9da72c9bb")
            `When I send a GET request`("/articles/65cda9f3-8991-4420-8d41-1da9da72c9bb") `Then the response should be` OK and {
                `And the response should not be null`
                `And have property`("$.id") `whish contains` "65cda9f3-8991-4420-8d41-1da9da72c9bb"
            }
        }
    }

    @Test
    fun `I can create an article`() {
        withIntegrationApplication {
            `Given I have citizen`("John", "Doe")
            `When I send a POST request`("/articles") {
                `authenticated as`("John", "Doe")
                """
                {
                  "version_id": "09c418b6-63ba-448b-b38b-502b41cd500e",
                  "title": "title2",
                  "annonymous": false,
                  "content": "content2",
                  "description": "description2",
                  "tags": [
                      "green"
                  ]
                }
                """
            } `Then the response should be` OK and {
                `And the response should not be null`
                `And have property`("$.version_id") `whish contains` "09c418b6-63ba-448b-b38b-502b41cd500e"
                `And have property`("$.title") `whish contains` "title2"
            }
        }
    }
}
