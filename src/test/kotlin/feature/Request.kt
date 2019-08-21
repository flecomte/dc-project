package feature

import com.google.gson.Gson
import com.google.gson.JsonParser
import cucumber.api.java8.En
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.User
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.cucumber.datatable.DataTable
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.setBody
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.koin.test.KoinTest
import org.koin.test.inject
import org.opentest4j.AssertionFailedError
import java.util.*
import kotlin.random.Random
import kotlin.test.assertTrue
import kotlin.test.asserter
import feature.Context.Companion.current as currentContext

class Request: En, KoinTest {
    private val migrations: Migrations  by inject()
    private val requester: Requester  by inject()
    init {
        When("I have citizen:") { body: DataTable ->
            val user = User(username = "jaque_${Random.nextInt(0, 10000)}", plainPassword = "azerty")
            val test: TestApplicationEngine.() -> Unit = {
                requester
                    .getFunction("insert_user")
                    .selectOne(user)

                val data = body.asMap<String, String>(String::class.java, String::class.java)
                val citizen = Citizen(
                    id = UUID.fromString(data["id"]),
                    name = Citizen.Name(data["firstName"], data["lastName"]),
                    birthday = DateTime.now(),
                    user = user
                )
                requester
                    .getFunction("upsert_citizen")
                    .selectOne(citizen)
            }

            currentContext.engine.test()
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
                assertEquals(HttpStatusCode.fromValue(statusCode), response.status(), response.content)
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
            val response = JsonParser().parse(p.content).getAsJsonObject()

            expected.asMap<String, String>(String::class.java, String::class.java).forEach { (key, valueExpected) ->
                assertTrue(response.has(key))
                assertEquals(valueExpected, response.get(key).asString)
            }
        }
    }
}