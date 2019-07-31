package feature

import com.google.gson.Gson
import cucumber.api.Scenario
import cucumber.api.java8.En
import io.cucumber.datatable.DataTable
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.setBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.koin.test.KoinTest
import org.opentest4j.AssertionFailedError
import kotlin.test.asserter
import feature.Context.Companion.current as currentContext

class Request: En, KoinTest {
    init {
        Before { scenario: Scenario ->
        }

        After { scenario: Scenario ->
        }

        When("I send a {string} request to {string} with body:") { method: String, uri: String, body: String ->
            val test: TestApplicationEngine.() -> Unit = {
                currentContext.call = handleRequest {
                    applyConfigurations()
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    this.method = HttpMethod.parse(method)
                    this.uri = uri
                    setBody(body)
                }
            }

            currentContext.engine.test()
        }

        When("I send a {string} request to {string}") { method: String, uri: String ->
            val test: TestApplicationEngine.() -> Unit = {
                currentContext.call = handleRequest {
                    applyConfigurations()
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    this.method = HttpMethod.parse(method.toUpperCase())
                    this.uri = uri
                }
            }

            currentContext.engine.test()
        }

        Then("the response status code should be {int}") { statusCode: Int ->
            val call: TestApplicationCall = currentContext.call ?: throw AssertionFailedError("No call", statusCode, null)
            with(call) {
                assertEquals(HttpStatusCode.fromValue(statusCode), response.status())
            }
        }

        And("the response should contain:") { expected: DataTable ->
            val call: TestApplicationCall = currentContext.call ?: throw AssertionFailedError("No call")
            val p = call.response
            val response = Gson().fromJson<List<Map<String, String>>>(p.content, List::class.java)

            expected.asMap<String, String>(String::class.java, String::class.java).forEach { (key, value) ->
                response.forEach {
                    if (it.containsKey(key)) {
                        assertEquals(it[key], value)
                        return@And
                }
                }
                asserter.fail("The response not contain $key field")
            }
        }

        And("the response should contain object:") { expected: DataTable ->
            val call: TestApplicationCall = currentContext.call ?: throw AssertionFailedError("No call")
            val p = call.response
            val response = Gson().fromJson<Map<String, String>>(p.content, Map::class.java)

            expected.asMap<String, String>(String::class.java, String::class.java).forEach { (key, value) ->
                if (response.containsKey(key)) {
                    assertEquals(value, response[key])
                    return@And
                }
                asserter.fail("The response not contain $key field")
            }
        }
    }
}