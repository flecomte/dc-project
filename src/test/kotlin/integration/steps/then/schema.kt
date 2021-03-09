package integration.steps.then

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import fr.dcproject.common.utils.getResource
import io.ktor.http.Url
import io.ktor.request.contentType
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.server.testing.TestApplicationResponse
import org.openapi4j.core.model.v3.OAI3
import org.openapi4j.parser.OpenApi3Parser
import org.openapi4j.schema.validator.ValidationContext
import org.openapi4j.schema.validator.ValidationData
import org.openapi4j.schema.validator.v3.SchemaValidator
import java.io.File
import kotlin.test.assertTrue

fun TestApplicationResponse.`And schema must be valid`(route: String? = null) {
    OpenApi3Parser().parse(File("/openapi2.yaml".getResource().toURI()), true).let { api ->
        val operation = call.request.httpMethod
        val uri = route ?: "/" + Url(call.request.uri).encodedPath
        val path = api.paths
            .keys
            .firstOrNull { uri.matches(it.replace("""\{[^{}]+}""".toRegex(), "[^/]+").toRegex()) }

        api.getPath(path)
            ?.getOperation(operation.value.toLowerCase())
            ?.apply {
                val mediaType = call.request.contentType()
                val status = call.response.status()
                getResponse(status?.value?.toString() ?: error("HttpStatus not found"))
                    ?.getContentMediaType(mediaType.toString())
                    ?.schema?.let { schema ->
                        val validationContext: ValidationContext<OAI3> = ValidationContext(api.context)
                        val jsonNode: JsonNode = schema.toNode()
                        val schemaValidator = SchemaValidator(validationContext, "", jsonNode)

                        val results = ValidationData<Unit>()
                        val mapper = ObjectMapper()
                        schemaValidator.validate(mapper.readTree(content), results)

                        assertTrue(results.isValid, results.results().toString())
                    }
                    ?: error("""No path found for "$operation $uri" for status ${status.value} with media type "$mediaType".""")
            }
            ?.apply {
                Url(call.request.uri).parameters.forEach { parameter: String, values: List<String> ->
                    getParametersIn(api.context, "query")
                        ?.firstOrNull { it.name == parameter }?.schema?.let { schema ->
                            val validationContext: ValidationContext<OAI3> = ValidationContext(api.context)
                            val jsonNode: JsonNode = schema.toNode()
                            val schemaValidator = SchemaValidator(validationContext, "", jsonNode)
                            val params = ValidationData<Unit>()
                            schemaValidator.validate(TextNode(values.first()), params)

                            assertTrue(params.isValid, params.results().toString())
                        }
                        ?: error("""No path found for "$operation $uri" for status "$parameter".""")
                }
            }
            ?: error("""No path found for "$operation $uri".""")
    }
}
