package feature

import com.jayway.jsonpath.JsonPath
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ImplicitReflectionSerializer
@KtorExperimentalAPI
class KtorServerRequestSteps : En {
    init {
        Given("Next request as headers:") { dataTable: DataTable ->
            KtorServerContext.defaultServer.addPreRequestSetup(false) {
                dataTable.asMap<String, String>(String::class.java, String::class.java).forEach { key, value ->
                    this.addHeader(key, value)
                }
            }
        }

        Given("I send a {word} request to {string} with body:") { method: String, uri: String, body: String ->
            KtorServerContext.defaultServer.handleRequest {
                this.method = HttpMethod.parse(method)
                this.uri = uri
                this.addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(body)
            }
        }

        Given("I send a {word} request to {string}") { method: String, uri: String ->
            KtorServerContext.defaultServer.handleRequest {
                this.method = HttpMethod.parse(method.toUpperCase())
                this.uri = uri
            }
        }

        Then("the response status code should be {int}") { statusCode: Int ->
            assertEquals(HttpStatusCode.fromValue(statusCode), KtorServerContext.defaultServer.call?.response?.status())
        }

        Then("the response should contain object:") { expected: DataTable ->
            expected.asMap<String, String>(String::class.java, String::class.java).forEach { (key, valueExpected) ->
                assertEquals(valueExpected, JsonPath.read<Any>(response, key)?.toString() ?: throw AssertionError("\"$key\" element not found on json response"))
            }
        }

        Then("the response should not contain object:") { expected: DataTable ->
            expected.asMap<String, String>(String::class.java, String::class.java).forEach { (key, valueExpected) ->
                assertNotEquals(valueExpected, JsonPath.read<Any>(response, key)?.toString() ?: throw AssertionError("\"$key\" element not found on json response"))
            }
        }

        Then("print last response") {
            print(KtorServerContext.defaultServer.call?.response?.content)
        }
    }

    private val response: String?
        get() = KtorServerContext.defaultServer.call?.response?.content
}
