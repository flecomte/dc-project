package feature

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
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
import kotlin.test.fail

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
                val jsonPrimitive = findJsonElement(key) as? JsonPrimitive ?: fail("\"$key\" element isn't json primitive")
                assertEquals(valueExpected, jsonPrimitive.asString)
            }
        }

        Then("print last response") {
            print(KtorServerContext.defaultServer.call?.response?.content)
        }
    }

    private fun findJsonElement(node: String): JsonElement {
        var jsonElement: JsonElement = responseJsonElement
        val elements = node.split("].", "[", ".")

        elements
            .filter { it.trim().isNotBlank() }
            .map { it.trim() }
            .forEach {
                val asArrayIndex = """^\d+$""".toRegex().find(it)

                jsonElement = if (asArrayIndex != null) {
                    val index = asArrayIndex.groups.first()!!
                    jsonElement.asJsonArray.get(index.value.toInt())
                } else {
                    jsonElement.asJsonObject.get(it) ?: throw AssertionError("\"$node\" element not found on json response")
                }
            }

        return jsonElement
    }

    private val responseJsonElement: JsonElement
        get() = JsonParser().parse(KtorServerContext.defaultServer.call?.response?.content).getAsJsonObject() ?: fail("The response isn't valid JSON")
}
