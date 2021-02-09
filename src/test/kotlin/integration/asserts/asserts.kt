package integration.asserts

import com.jayway.jsonpath.JsonPath
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationResponse
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.shouldContain
import kotlin.test.assertEquals

fun TestApplicationCall.`Then the response should be`(status: HttpStatusCode? = null, block: TestApplicationResponse.() -> Unit): TestApplicationCall = this.apply {
    if (status != null) {
        response.status().`should be`(status)
    }
    block(response)
}

infix fun TestApplicationCall.`Then the response should be`(status: HttpStatusCode): TestApplicationCall = this.apply {
    response.status().`should be`(status)
}

infix fun TestApplicationCall.and(block: TestApplicationResponse.() -> Unit): TestApplicationCall = this.apply {
    block(response)
}

infix fun TestApplicationCall.`has property`(path: String): Pair<JsonPath, Any> =
    JsonPath.compile(path).let { jsonPath ->
        jsonPath.read<Any>(response.content)?.let { result ->
            Pair(jsonPath, result)
        } ?: throw AssertionError("\"${path}\" element not found on json response")
    }

infix fun TestApplicationResponse.`And have property`(path: String): Pair<JsonPath, Any> =
    JsonPath.compile(path).let { jsonPath ->
        jsonPath.read<Any>(content)?.let { result ->
            Pair(jsonPath, result)
        } ?: throw AssertionError("\"${path}\" element not found on json response")
    }

infix fun Pair<JsonPath, Any>.`whish contains`(expected: Any): Pair<JsonPath, Any> = this.apply {
    expected `should be equal to` second
}

fun TestApplicationResponse.`And the response should contain`(path: String, valueExpected: String) {
    assertEquals(valueExpected, JsonPath.read<Any>(content, path)?.toString() ?: throw AssertionError("\"$path -> ${valueExpected}\" element not found on json response"))
}

val TestApplicationResponse.`And the response should not be null` get() = content.`should not be null`()
infix fun String.`and should contains`(expected: String) = this
    .`should not be null`()
    .shouldContain(expected)
