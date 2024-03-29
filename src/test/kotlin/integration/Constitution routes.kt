package integration

import integration.steps.given.`Given I have citizen`
import integration.steps.given.`Given I have constitution`
import integration.steps.given.`Given I have constitutions`
import integration.steps.given.`authenticated as`
import integration.steps.then.`And have property`
import integration.steps.then.`And the response should contain`
import integration.steps.then.`And the response should not be null`
import integration.steps.then.`Then the response should be`
import integration.steps.then.and
import integration.steps.then.`which contains`
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
@Tags(Tag("integration"), Tag("constitution"))
class `Constitution routes` : BaseTest() {
    @Test
    fun `I can get constitution list`() {
        withIntegrationApplication {
            `Given I have constitutions`(3)
            `When I send a GET request`("/constitutions?page=1&limit=10&sort=title&direction=desc") `Then the response should be` OK and {
                `And the response should not be null`()
            }
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot get constitution list with wrong request`() {
        withIntegrationApplication {
            `Given I have constitutions`(3)
            `When I send a GET request`("/constitutions?page=1&limit=5000&sort=title&direction=desc", ALL - REQUEST_PARAM) `Then the response should be` BadRequest and {
                `And the response should not be null`()
                `And the response should contain`("$.invalidParams[0].name", ".limit")
                `And the response should contain`("$.invalidParams[0].reason", "must be at most '50'")
            }
        }
    }

    @Test
    fun `I can get constitution by ID`() {
        withIntegrationApplication {
            `Given I have constitutions`(3)
            `Given I have constitution`("0321c8d1-4ce3-4763-b5f4-a92611d280b4")
            `When I send a GET request`("/constitutions/0321c8d1-4ce3-4763-b5f4-a92611d280b4") `Then the response should be` OK and {
                `And the response should not be null`()
                `And have property`("$.id") `which contains` "0321c8d1-4ce3-4763-b5f4-a92611d280b4"
            }
        }
    }

// TODO implement constitution versions

//    @Test
//    fun `I can get versions of constitution by the id`() {
//        withIntegrationApplication {
//            `Given I have constitution`(id = "aeefee97-900a-4336-a55a-6b171fc83b73")
//            `When I send a GET request`("/constitutions/aeefee97-900a-4336-a55a-6b171fc83b73/versions") `Then the response should be` OK and {
//                `And the response should not be null`()
//                `And have property`("$.total") `whish contains` 1
//                `And have property`("$.result[0].id") `whish contains` "aeefee97-900a-4336-a55a-6b171fc83b73"
//            }
//        }
//    }

    @Test
    fun `I can create an constitution`() {
        withIntegrationApplication {
            `Given I have citizen`("Henri", "Poincaré")
            `When I send a POST request`("/constitutions") {
                `authenticated as`("Henri", "Poincaré")
                `with body`(
                    """
                    {
                       "versionId":"15814bb6-8d90-4c6a-a456-c3939a8ec75e",
                       "title":"Cras sit amet sapien mattis nulla rutrum blandit.",
                       "anonymous":true,
                       "titles":[
                          {
                             "name":"Cras sit amet sapien mattis nulla rutrum blandit."
                          }
                       ]
                    }
                    """
                )
            } `Then the response should be` Created and {
                `And the response should not be null`()
                `And have property`("$.versionId") `which contains` "15814bb6-8d90-4c6a-a456-c3939a8ec75e"
                `And have property`("$.title") `which contains` "Cras sit amet sapien mattis nulla rutrum blandit."
            }
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot create an constitution if bad request`() {
        withIntegrationApplication {
            `Given I have citizen`("Henri", "Poincaré")
            `When I send a POST request`("/constitutions", ALL - REQUEST_BODY) {
                `authenticated as`("Henri", "Poincaré")
                `with body`(
                    """
                    {
                       "versionId":"15814bb6-8d90-4c6a-a456-c3939a8ec75e",
                       "title":"Hello world!",
                       "anonymous":true,
                       "titles":[
                          {
                             "name":"plop",
                             "wrongField":0
                          }
                       ]
                    }
                    """
                )
            } `Then the response should be` BadRequest
        }
    }

    @Test
    @Tag("BadRequest")
    fun `I cannot create an constitution if request is not valid`() {
        withIntegrationApplication {
            `Given I have citizen`("Henri", "Poincaré")
            `When I send a POST request`("/constitutions", ALL - REQUEST_BODY) {
                `authenticated as`("Henri", "Poincaré")
                `with body`(
                    """
                    {
                       "versionId":"15814bb6-8d90-4c6a-a456-c3939a8ec75e",
                       "title":"too small",
                       "anonymous":true,
                       "titles":[
                          {
                             "name":"too small"
                          }
                       ]
                    }
                    """
                )
            } `Then the response should be` BadRequest and {
                `And the response should contain`("$.invalidParams[0].name", ".title")
                `And the response should contain`("$.invalidParams[0].reason", "must have at least 10 characters")
                `And the response should contain`("$.invalidParams[1].name", ".titles[0].name")
                `And the response should contain`("$.invalidParams[1].reason", "must have at least 10 characters")
            }
        }
    }
}
