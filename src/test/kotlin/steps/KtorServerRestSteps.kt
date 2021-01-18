package steps

import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class KtorServerRestSteps : En {
    init {
        Then("the JSON should contain:") { dataTable: DataTable ->
            dataTable.asMap<String, String>(String::class.java, String::class.java).forEach { (key, value) ->
                val jsonPrimitive = findJsonElement(key) as? JsonPrimitive ?: fail("\"$key\" element isn't json primitive")
                assertEquals(jsonPrimitive.content, value)
            }
        }

        Then("the JSON element {word} should have {int} item(s)") { node: String, count: Int ->
            val jsonArray = findJsonElement(node) as? JsonArray ?: fail("\"$node\" element isn't json array")
            assertEquals(count, jsonArray.size)
        }

        Then("the JSON should have {int} item(s)") { count: Int ->
            val jsonArray = responseJsonElement as? JsonArray ?: fail("The json response isn't array")
            assertEquals(count, jsonArray.size)
        }

        Then("the Response should be:") { body: String ->
            assertEquals(body, response)
        }

        Then("the Response should contain:") { body: String ->
            assertTrue(response.contains(body))
        }
    }

    private fun findJsonElement(path: String): JsonElement {
        var jsonElement: JsonElement = responseJsonElement

        path
            .split("].", "]", "[", ".")
            .filter { it.trim().isNotBlank() }
            .map { it.trim() }
            .forEach {
                jsonElement = if (jsonElement is JsonArray) {
                    jsonElement.jsonArray[it.toInt()]
                } else {
                    jsonElement.jsonObject[it]
                } ?: throw AssertionError("\"$path\" element not found on json response")
            }

        return jsonElement
    }

    private val responseJsonElement: JsonElement
        get() = Json.parseToJsonElement(KtorServerContext.defaultServer.call?.response?.content ?: fail("The response isn't valid JSON"))

    private val response: String
        get() = KtorServerContext.defaultServer.call?.response?.content ?: fail("The response isn't valid")
}
