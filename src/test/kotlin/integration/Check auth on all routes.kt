package integration

import fr.dcproject.common.utils.getResource
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openapi4j.core.model.OAIContext
import org.openapi4j.parser.OpenApi3Parser
import org.openapi4j.parser.model.v3.OpenApi3
import org.openapi4j.parser.model.v3.Operation
import org.openapi4j.parser.model.v3.Parameter
import org.openapi4j.parser.model.v3.Path
import java.io.File
import java.util.UUID
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tags(Tag("integration"), Tag("auth"))
class `Check auth on all routes` : BaseTest() {
    @Test
    fun `Check all routes`() {
        val filePath = "/openapi.yaml"
        OpenApi3Parser().parse(File(filePath.getResource().toURI()), true).let { api: OpenApi3 ->
            /* Loop on paths and http methods */
            api.paths.flatMap { (pathName: String, path: Path) ->
                path.operations
                    /* Take only the secure route */
                    .filter { (_, operation: Operation) -> operation.hasSecurityRequirements() }
                    .map { (methodName, _) ->
                        /* Send request to check security */
                        sendRequest(
                            path.buildUrl(pathName, methodName, api.context), /* Replace route to real URL */
                            HttpMethod.parse(methodName.toUpperCase()) /* Convert http method name to enum */
                        )
                    }
            }.let { requests ->
                /* Check security of routes */
                assertTrue(
                    requests.all { it.statusCode == HttpStatusCode.Forbidden },
                    requests
                        .filter { it.statusCode != HttpStatusCode.Forbidden }
                        .joinToString("\n") { it.toString() }
                )
            }
        }
    }

    private fun sendRequest(uri: String, method: HttpMethod): RequestResponse {
        return try {
            withIntegrationApplication {
                handleRequest(true) {
                    this.method = method
                    this.uri = uri
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                }.run {
                    RequestResponse(
                        response.status() ?: error("Request error"),
                        method,
                        uri
                    )
                }
            }
        } catch (e: Throwable) {
            RequestResponse(
                HttpStatusCode.InternalServerError,
                method,
                uri
            )
        }
    }

    private data class RequestResponse(
        val statusCode: HttpStatusCode,
        val method: HttpMethod,
        val uri: String
    ) {
        override fun toString(): String {
            return """HttpStatus ${statusCode.value} for: ${method.value.padStart(6, ' ')} $uri"""
        }
    }
}

private fun Path.buildUrl(path: String, methodName: String, context: OAIContext): String {
    val urlReplaced = this.getParametersIn(context, "path")
        .fold(path) { pathToReplace: String, parameter: Parameter ->
            """\{${parameter.name}}""".toRegex().replace(
                pathToReplace,
                parameter.generateFakeValue()
            )
        }

    val rootQueryParameters = this.getParametersIn(context, "query")
        .filter { it.isRequired }
        .map { parameter ->
            parameter
                .generateFakeArray()
                .joinToString("&") { "${parameter.name}=$it" }
        }

    val queryParameters = this.getOperation(methodName).getParametersIn(context, "query")
        .filter { it.isRequired }
        .map { parameter ->
            parameter
                .generateFakeArray()
                .joinToString("&") { "${parameter.name}=$it" }
        }
    val allParameters: String = (rootQueryParameters + queryParameters)
        .joinToString("&")
        .let {
            if (it.isNotEmpty()) {
                "?$it"
            } else {
                it
            }
        }

    return "$urlReplaced$allParameters"
}

private fun Parameter.generateFakeValue(): String {
    return if (example != null) {
        example.toString()
    } else if (schema.type == "string" && schema.format == "uuid") {
        UUID.randomUUID().toString()
    } else {
        "example123"
    }
}

private fun Parameter.generateFakeArray(): List<String> {
    if (schema.type != "array") {
        error("Parameter is not an array")
    }
    return if (example != null && example is Iterable<*>) {
        (example as Iterable<*>).map { it.toString() }
    } else if (schema.itemsSchema.type == "string" && schema.itemsSchema.format == "uuid") {
        listOf(UUID.randomUUID().toString())
    } else {
        listOf("example123")
    }
}
