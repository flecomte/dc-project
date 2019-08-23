package feature

import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.parse
import kotlin.test.assertEquals
import kotlin.test.fail

@ImplicitReflectionSerializer
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
    }

    private fun findJsonElement(node: String): JsonElement {
        var jsonElement: JsonElement = responseJsonElement
        val elements = node.split(".")

        elements.forEach {
            val asArrayIndex = """\d+""".toRegex().find(it)

            jsonElement = if (asArrayIndex != null) {
                val index = asArrayIndex.groups.first()!!
                jsonElement.jsonArray.get(index.value.toInt())
            } else {
                jsonElement.jsonObject.get(it) ?: throw AssertionError("\"$node\" element not found on json response")
            }
        }

        return jsonElement
    }

    private val responseJsonElement: JsonElement
        get() = Json.parse(KtorServerContext.defaultServer.call?.response?.content ?: fail("The response isn't valid JSON"))
}
