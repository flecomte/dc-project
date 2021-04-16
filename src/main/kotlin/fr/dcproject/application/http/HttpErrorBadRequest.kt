package fr.dcproject.application.http

import fr.dcproject.application.http.HttpErrorBadRequest.InvalidParam
import io.konform.validation.ValidationResult
import io.ktor.http.HttpStatusCode

class BadRequestException(val httpError: HttpErrorBadRequest) : Exception()

class HttpErrorBadRequest(
    statusCode: HttpStatusCode,
    val title: String = statusCode.description,
    val invalidParams: List<InvalidParam>,
) {
    val statusCode: Int = statusCode.value
    data class InvalidParam(
        val name: String,
        val reason: String
    )
}

fun ValidationResult<*>.toOutput() = HttpErrorBadRequest(
    HttpStatusCode.BadRequest,
    invalidParams = this.errors.map {
        InvalidParam(
            it.dataPath,
            it.message
        )
    }
)

fun ValidationResult<*>.badRequestIfNotValid() {
    if (errors.size > 0) {
        throw BadRequestException(toOutput())
    }
}
