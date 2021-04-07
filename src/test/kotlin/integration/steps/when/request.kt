package integration.steps.`when`

import fr.dcproject.common.BitMaskI
import integration.steps.then.`And the schema parameters must be valid`
import integration.steps.then.`And the schema request body must be valid`
import integration.steps.then.`And the schema response body must be valid`
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.setBody

enum class Validate(override val bit: Long) : BitMaskI {
    NONE(0),
    REQUEST_BODY(1),
    REQUEST_PARAM(2),
    REQUEST_HEADER(4),
    REQUEST(1 + 2 + 4),
    RESPONSE_BODY(8),
    RESPONSE_HEADER(16),
    RESPONSE(8 + 16),
    ALL((1 + 2 + 4) + (8 + 16));
}

fun TestApplicationCall.valid(validate: BitMaskI): TestApplicationCall {
    if (Validate.RESPONSE_BODY in validate) {
        response.`And the schema response body must be valid`()
    }
    if (Validate.REQUEST_PARAM in validate) {
        response.`And the schema parameters must be valid`()
    }
    if (Validate.REQUEST_BODY in validate) {
        requestBody?.let { body ->
            response.`And the schema request body must be valid`(body)
        }
    }

    return this
}

fun TestApplicationEngine.`When I send a GET request`(uri: String? = null, validate: BitMaskI = Validate.ALL, setup: (TestApplicationRequest.() -> Unit)? = null): TestApplicationCall {
    return handleRequest(true) {
        method = HttpMethod.Get
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setup?.let { it() }
    }.valid(validate)
}

fun TestApplicationEngine.`When I send a POST request`(uri: String? = null, validate: BitMaskI = Validate.ALL, setup: (TestApplicationRequest.() -> Unit)? = null): TestApplicationCall {
    return handleRequest(true) {
        method = HttpMethod.Post
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
        setup?.let { it() }
    }.valid(validate)
}

fun TestApplicationEngine.`When I send a PUT request`(uri: String? = null, validate: BitMaskI = Validate.ALL, setup: (TestApplicationRequest.() -> Unit)? = null): TestApplicationCall {
    return handleRequest(true) {
        method = HttpMethod.Put
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setup?.let { it() }
    }.valid(validate)
}

fun TestApplicationEngine.`When I send a DELETE request`(uri: String? = null, validate: BitMaskI = Validate.ALL, setup: (TestApplicationRequest.() -> String?)? = null): TestApplicationCall {
    return handleRequest(true) {
        method = HttpMethod.Delete
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setup?.let { it() }?.let {
            setBody(it.trimIndent())
        }
    }.valid(validate)
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
