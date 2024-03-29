package integration.steps.then

import assert.assertContain
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationResponse
import net.minidev.json.JSONArray
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

infix fun Pair<JsonPath, Any>.`which contains`(expected: Any): Pair<JsonPath, Any> = this.apply {
    second `should be equal to` expected
}

inline fun <reified T> TestApplicationResponse.`And the response should contain`(path: String, valueExpected: T?): T {
    return JsonPath.read<T?>(content, path).also {
        it.let {
            if (it is JSONArray && it.count() == 1 && it.first() is T?) {
                it.first() as T?
            } else {
                it
            }
        }.let {
            assertEquals<T?>(valueExpected, it ?: throw AssertionError("\"$path -> ${valueExpected}\" element not found on json response"))
        }
    }
}

fun TestApplicationResponse.`And the response should not contain`(path: String) {
    assertThrows<PathNotFoundException> {
        JsonPath.read(content, path)
    }
}

fun TestApplicationResponse.`And the response should contain pattern`(path: String, expectedRegex: String): Any {
    return JsonPath.read<Any?>(content, path).also {
        it.let {
            if (it is JSONArray && it.count() == 1) {
                it.first()
            } else {
                it
            }
        }?.let {
            assertTrue(expectedRegex.toRegex().containsMatchIn(it.toString()))
        } ?: throw AssertionError("\"$path -> element not found on json response")
    }
}

fun TestApplicationResponse.`And the response should contain list`(path: String, exactCount: Int) =
    `And the response should contain list`(path, IntRange(exactCount, exactCount))

fun TestApplicationResponse.`And the response should contain list`(path: String, range: IntRange) {
    JsonPath.read<JSONArray?>(content, path).also {
        assertNotNull(it)
        range assertContain it.size
    }
}

fun TestApplicationResponse.`And the response should not be null`() = content.`should not be null`()
fun TestApplicationResponse.`And the response should be null`() = content.`should be null`()
infix fun String.`and should contains`(expected: String) = this
    .`should not be null`()
    .shouldContain(expected)
