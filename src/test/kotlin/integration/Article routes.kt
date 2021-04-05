package integration

import fr.dcproject.common.utils.toUUID
import integration.steps.`when`.Validate
import integration.steps.`when`.`When I send a GET request`
import integration.steps.`when`.`When I send a POST request`
import integration.steps.`when`.`with body`
import integration.steps.given.`Given I have article created by workgroup`
import integration.steps.given.`Given I have article`
import integration.steps.given.`Given I have articles`
import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have workgroup`
import integration.steps.given.`authenticated as`
import integration.steps.then.`And have property`
import integration.steps.then.`And the response should contain list`
import integration.steps.then.`And the response should contain pattern`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`And the response should not contain`
import integration.steps.then.`Then the response should be`
import integration.steps.then.`which contains`
import integration.steps.then.and
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("article"))
class `Article routes` : BaseTest() {
    @Test
    fun `I can get article list`() {
        withIntegrationApplication {
            `Given I have articles`(3)
            `Given I have article`(createdBy = "ddb17f17-e8ab-4ada-bdf7-bfd6b0f1b5ed".toUUID())
            `When I send a GET request`("/articles?page=1&limit=10&sort=title&createdBy=ddb17f17-e8ab-4ada-bdf7-bfd6b0f1b5ed") `Then the response should be` OK and {
                `And the response should not be null`()
                `And the response should contain pattern`("$.result[0].createdBy.name.firstName", "firstName.+")
                `And the response should not contain`("$.result[1]")
                `And the response should contain list`("$.result", 1)
            }
        }
    }

    @Test
    @Tag("Validation")
    fun `I cannot get article list`() {
        withIntegrationApplication {
            `Given I have articles`(3)
            `When I send a GET request`("/articles?page=1&limit=10&sort=title&createdBy=hello", Validate.ALL - Validate.REQUEST_PARAM) `Then the response should be` BadRequest and {
                `And the response should contain`("$.invalidParams[*].name", ".createdBy")
                `And the response should contain`("$.invalidParams[*].reason", "must be UUID")
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
                `And the response should not be null`()
                `And have property`("$.total") `which contains` 1
                `And have property`("$.result[0]workgroup.name") `which contains` "Les papy"
            }
        }
    }

    @Test
    fun `I can get article by id`() {
        withIntegrationApplication {
            `Given I have article`(id = "65cda9f3-8991-4420-8d41-1da9da72c9bb")
            `When I send a GET request`("/articles/65cda9f3-8991-4420-8d41-1da9da72c9bb") `Then the response should be` OK and {
                `And the response should not be null`()
                `And have property`("$.id") `which contains` "65cda9f3-8991-4420-8d41-1da9da72c9bb"
            }
        }
    }

    @Test
    fun `I can get versions of article by the id`() {
        withIntegrationApplication {
            `Given I have article`(id = "13e6091c-8fed-4600-b079-a97a6b7a9800")
            `When I send a GET request`("/articles/13e6091c-8fed-4600-b079-a97a6b7a9800/versions?page=1&limit=10&sort=title") `Then the response should be` OK and {
                `And the response should not be null`()
                `And have property`("$.total") `which contains` 1
                `And have property`("$.result[0].id") `which contains` "13e6091c-8fed-4600-b079-a97a6b7a9800"
            }
        }
    }

    @Test
    @Tag("Validation")
    fun `I cannot get versions of article by the id with wrong id`() {
        withIntegrationApplication {
            `Given I have article`(id = "13e6091c-8fed-4600-b079-a97a6b7a9800")
            `When I send a GET request`("/articles/abcd/versions") `Then the response should be` BadRequest and {
                `And the response should not be null`()
                `And the response should contain`("$.invalidParams[0].name", ".article")
                `And the response should contain`("$.invalidParams[0].reason", "must be UUID")
            }
        }
    }

    @Test
    @Tag("Validation")
    fun `I cannot get versions of article by the id with wrong request`() {
        withIntegrationApplication {
            `Given I have article`(id = "13e6091c-8fed-4600-b079-a97a6b7a9800")
            `When I send a GET request`("/articles/13e6091c-8fed-4600-b079-a97a6b7a9800/versions?page=1&limit=10&sort=wrong") `Then the response should be` BadRequest and {
                `And the response should not be null`()
                `And the response should contain`("$.invalidParams[0].name", ".sort")
                `And the response should contain pattern`("$.invalidParams[0].reason", "must be one of: ('[^']+'(, )?)+")
            }
        }
    }

    @Test
    fun `I can create an article`() {
        withIntegrationApplication {
            `Given I have citizen`("John", "Doe")
            `When I send a POST request`("/articles") {
                `authenticated as`("John", "Doe")
                `with body`(
                    """
                    {
                      "versionId": "09c418b6-63ba-448b-b38b-502b41cd500e",
                      "title": "title2",
                      "anonymous": false,
                      "content": "content2",
                      "description": "description2",
                      "tags": [
                          "green"
                      ]
                    }
                    """
                )
            } `Then the response should be` OK and {
                `And the response should not be null`()
                `And have property`("$.versionId") `which contains` "09c418b6-63ba-448b-b38b-502b41cd500e"
            }
        }
    }

    @Test
    fun `I cannot create an article if I'm not connected`() {
        withIntegrationApplication {
            `When I send a POST request`("/articles") {
                `with body`(
                    """
                    {
                      "versionId": "e3c7ce42-241c-4caf-9a59-aba4e466440e",
                      "title": "title2",
                      "anonymous": false,
                      "content": "content2",
                      "description": "description2",
                      "tags": [
                          "green"
                      ]
                    }
                    """
                )
            } `Then the response should be` Forbidden and {
                `And the response should not be null`()
                `And the response should contain`("$.statusCode", 403)
                `And the response should contain`("$.title", "No User Connected")
            }
        }
    }
}
