package feature

import com.google.gson.JsonParser
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.jupiter.api.Assertions
import org.opentest4j.AssertionFailedError
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
            val call = KtorServerContext.defaultServer.call ?: throw AssertionFailedError("No call")
            val response = JsonParser().parse(call.response.content).getAsJsonObject()

            expected.asMap<String, String>(String::class.java, String::class.java).forEach { (key, valueExpected) ->
                assertTrue(response.has(key))
                Assertions.assertEquals(valueExpected, response.get(key).asString)
            }
        }

        Then("print last response") {
            print(KtorServerContext.defaultServer.call?.response?.content)
        }
    }
}
