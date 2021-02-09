package integration.asserts.`when`

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.setBody

public fun TestApplicationEngine.`When I send a GET request`(uri: String? = null, setup: (TestApplicationRequest.() -> Unit)? = null): TestApplicationCall {
    val setupOveride: TestApplicationRequest.() -> Unit = {
        method = HttpMethod.Get
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setup?.let { it() }
    }
    return handleRequest(true, setupOveride)
}

public fun TestApplicationEngine.`When I send a POST request`(uri: String? = null, setup: (TestApplicationRequest.() -> String?)? = null): TestApplicationCall {
    val setupOveride: TestApplicationRequest.() -> Unit = {
        method = HttpMethod.Post
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setup?.let { it() }?.let {
            setBody(it.trimIndent())
        }
    }
    return handleRequest(true, setupOveride)
}

public fun TestApplicationEngine.`When I send a PUT request`(uri: String? = null, setup: (TestApplicationRequest.() -> String?)? = null): TestApplicationCall {
    val setupOveride: TestApplicationRequest.() -> Unit = {
        method = HttpMethod.Put
        if (uri != null) {
            this.uri = uri
        }
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setup?.let { it() }?.let {
            setBody(it.trimIndent())
        }
    }
    return handleRequest(true, setupOveride)
}

fun TestApplicationRequest.`with body`(body: String) {
    setBody(body.trimIndent())
}
