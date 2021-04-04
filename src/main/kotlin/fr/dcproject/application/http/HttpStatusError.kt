package fr.dcproject.application.http

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException
import fr.dcproject.common.security.AccessDeniedException
import fr.dcproject.component.auth.ForbiddenException
import fr.dcproject.component.auth.user
import io.konform.validation.ValidationResult
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import java.util.concurrent.CompletionException

class HttpError(
    statusCode: HttpStatusCode,
    val cause: Throwable? = null,
    val type: String? = null,
    val title: String = cause?.message ?: statusCode.description,
    val detail: String? = null,
    val invalidParams: List<InvalidParam>? = null,
    val stackTrace: String? = cause?.stackTraceToString()
) {
    val statusCode: Int = statusCode.value
    data class InvalidParam(
        val name: String,
        val reason: String
    )
}

fun ValidationResult<*>.toOutput(): HttpError {
    return HttpError(
        HttpStatusCode.BadRequest,
        invalidParams = this.errors.map {
            HttpError.InvalidParam(
                it.dataPath,
                it.message
            )
        }
    )
}

suspend fun PipelineContext<*, ApplicationCall>.respondIfNotValid(validationResult: ValidationResult<*>): HttpError? {
    if (validationResult.errors.size > 0) {
        val out = validationResult.toOutput()
        this.call.respond(
            HttpStatusCode.BadRequest,
            out
        )
        return out
    }
    return null
}

fun statusPagesInstallation(): StatusPages.Configuration.() -> Unit = {
    exception<CompletionException> { e ->
        val parent = e.cause?.cause
        if (parent is GenericDatabaseException) {
            HttpError(
                HttpStatusCode.BadRequest,
                cause = parent
            ).let {
                call.respond(HttpStatusCode.BadRequest, it)
            }
        } else {
            HttpError(
                HttpStatusCode.BadRequest,
                cause = e
            ).let {
                call.respond(HttpStatusCode.InternalServerError, it)
            }
        }
    }
    exception<NotFoundException> { e ->
        HttpError(
            HttpStatusCode.NotFound,
            cause = e
        ).let {
            call.respond(HttpStatusCode.NotFound, it)
        }
    }
    exception<AccessDeniedException> { e ->
        if (call.user == null) {
            HttpError(
                HttpStatusCode.Unauthorized,
                cause = e
            ).let {
                call.respond(HttpStatusCode.Unauthorized, it)
            }
        } else {
            HttpError(
                HttpStatusCode.Forbidden,
                cause = e
            ).let {
                call.respond(HttpStatusCode.Forbidden, it)
            }
        }
    }
    exception<ForbiddenException> { e ->
        HttpError(
            HttpStatusCode.Forbidden,
            cause = e
        ).let {
            call.respond(HttpStatusCode.Forbidden, it)
        }
    }
}
