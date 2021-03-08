package integration.steps.then

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fr.dcproject.common.utils.getResource
import io.ktor.request.contentType
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.server.testing.TestApplicationResponse
import org.openapi4j.core.model.v3.OAI3
import org.openapi4j.parser.OpenApi3Parser
import org.openapi4j.parser.model.v3.OpenApi3
import org.openapi4j.parser.model.v3.Schema
import org.openapi4j.schema.validator.ValidationContext
import org.openapi4j.schema.validator.ValidationData
import org.openapi4j.schema.validator.v3.SchemaValidator
import java.io.File
import kotlin.test.assertTrue

fun TestApplicationResponse.`And schema must be valid`() {
    // Parse without validation, setting to true is strongly recommended for further data validation.
    val api: OpenApi3 = OpenApi3Parser().parse(File("/openapi2.yaml".getResource().toURI()), true)

    val mediaType = this.call.request.contentType()
    val operation = this.call.request.httpMethod
    val uri = this.call.request.uri
    val status = this.call.response.status()

    val schema: Schema = api
        .getPath(uri)
        .getOperation(operation.value.toLowerCase())
        .getResponse(status?.value?.toString() ?: error("HttpStatus not found"))
        .getContentMediaType(mediaType.toString())
        .schema

    val validationContext: ValidationContext<OAI3> = ValidationContext(api.context)
    val jsonNode: JsonNode = schema.toNode()
    val schemaValidator = SchemaValidator(validationContext, "", jsonNode)

    val mapper = ObjectMapper()
    val results = ValidationData<Unit>()
    schemaValidator.validate(mapper.readTree(content), results)

    assertTrue(results.isValid, results.results().toString())
}
