package integration.steps.then

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import fr.dcproject.common.utils.getResource
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.request.contentType
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.server.testing.TestApplicationResponse
import org.openapi4j.core.model.v3.OAI3
import org.openapi4j.parser.OpenApi3Parser
import org.openapi4j.parser.model.v3.OpenApi3
import org.openapi4j.parser.model.v3.Operation
import org.openapi4j.parser.model.v3.Schema
import org.openapi4j.schema.validator.ValidationContext
import org.openapi4j.schema.validator.ValidationData
import org.openapi4j.schema.validator.v3.SchemaValidator
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.fail

fun Schema.validate(api: OpenApi3, toValidate: JsonNode) {
    val validationContext: ValidationContext<OAI3> = ValidationContext(api.context)
    val schemaValidator = SchemaValidator(validationContext, "", this.toNode())

    val results = ValidationData<Unit>()
    schemaValidator.validate(toValidate, results)

    assertTrue(results.isValid, results.results().toString())
}

fun TestApplicationResponse.operation(route: String? = null, callback: Operation.(OpenApi3, String) -> Unit): Operation {
    val filePath = "/openapi2.yaml"
    return OpenApi3Parser().parse(File(filePath.getResource().toURI()), true).let { api: OpenApi3 ->
        val httpMethod = call.request.httpMethod
        val uri = route ?: "/" + Url(call.request.uri).encodedPath
        val path = api.paths
            .keys
            .firstOrNull { uri.matches(it.replace("""\{[^{}]+}""".toRegex(), "[^/]+").toRegex()) }

        api.getPath(path)
            ?.getOperation(httpMethod.value.toLowerCase())
            ?.apply {
                this.callback(api, uri)
            }
            ?: fail("""No path found for "${httpMethod.value} $uri". (on file "$filePath")""")
    }
}

fun TestApplicationResponse.`And the schema response body must be valid`(contentType: ContentType? = ContentType.Application.Json) {
    operation { api, uri ->
        /* Validate Response */
        this.apply {
            val status = call.response.status()
            val responseContent: JsonNode = if (content != null)
                ObjectMapper().readTree(content)
            else TextNode("")

            val response = getResponse(status?.value?.toString() ?: error("HttpStatus not found")) ?: fail("""No Status "${status.value}" found for "$this $uri".""")
            val schema = response.getContentMediaType(contentType.toString())?.schema

            if (content != null) {
                schema?.validate(api, responseContent)
                    ?: fail("""No Status "${status.value}" found with media type "$contentType" for "$this $uri".""")
            }
        }
    }
}

fun TestApplicationResponse.`And the schema parameters must be valid`() {
    operation { api, uri ->
        /* Validate Request URL */
        this.apply {
            Url(call.request.uri).parameters.forEach { parameter: String, values: List<String> ->
                getParametersIn(api.context, "query")
                    ?.firstOrNull { it.name == parameter }?.schema
                    ?.validate(api, TextNode(values.first()))
                    ?: error("""No parameter found ($parameter) for "$this $uri".""")
            }
        }
    }
}

/**
 * Validate request body
 */
fun TestApplicationResponse.`And the schema request body must be valid`(body: String) {
    operation { api, uri ->
        requestBody
            .getContentMediaType(call.request.contentType().toString())
            .schema
            .validate(api, ObjectMapper().readTree(body))
    }
}
