package integration.steps.`when`

import integration.steps.then.`And the schema must be valid`
import integration.steps.then.`And the schema request body must be valid`
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.setBody

fun TestApplicationEngine.`When I send a GET request`(uri: String? = null, validate: Boolean = true, setup: (TestApplicationRequest.() -> Unit)? = null): TestApplicationCall {
    return handleRequest(true) {
        method = HttpMethod.Get
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setup?.let { it() }
    }.apply {
        if (validate) {
            response.`And the schema must be valid`()
            requestBody?.let { body ->
                response.`And the schema request body must be valid`(body)
            }
        }
    }
}

fun TestApplicationEngine.`When I send a POST request`(uri: String? = null, validate: Boolean = true, setup: (TestApplicationRequest.() -> Unit)? = null): TestApplicationCall {
    return handleRequest(true) {
        method = HttpMethod.Post
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
        setup?.let { it() }
    }.apply {
        if (validate) {
            response.`And the schema must be valid`()
            requestBody?.let { body ->
                response.`And the schema request body must be valid`(body)
            }
        }
    }
}

fun TestApplicationEngine.`When I send a PUT request`(uri: String? = null, validate: Boolean = true, setup: (TestApplicationRequest.() -> Unit)? = null): TestApplicationCall {
    return handleRequest(true) {
        method = HttpMethod.Put
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setup?.let { it() }
    }.apply {
        if (validate) {
            response.`And the schema must be valid`()
            requestBody?.let { body ->
                response.`And the schema request body must be valid`(body)
            }
        }
    }
}

fun TestApplicationEngine.`When I send a DELETE request`(uri: String? = null, validate: Boolean = true, setup: (TestApplicationRequest.() -> String?)? = null): TestApplicationCall {
    return handleRequest(true) {
        method = HttpMethod.Delete
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setup?.let { it() }?.let {
            setBody(it.trimIndent())
        }
    }.apply {
        if (validate) {
            response.`And the schema must be valid`()
            requestBody?.let { body ->
                response.`And the schema request body must be valid`(body)
            }
        }
    }
}

private val requestBodies: MutableMap<ApplicationCall, String> = mutableMapOf()
var TestApplicationCall.requestBody: String?
    get() = requestBodies[this]
    set(value) {
        if (value == null) {
            requestBodies.remove(this)
        } else {
            requestBodies[this] = value
        }
    }

infix fun TestApplicationRequest.`with body`(body: String) {
    return body.trimIndent().let {
        setBody(it)
        (call as TestApplicationCall).requestBody = it
    }
}
