package fr.dcproject.application.http

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException
import fr.dcproject.common.security.AccessDeniedException
import fr.dcproject.component.auth.ForbiddenException
import fr.dcproject.component.auth.user
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.features.ParameterConversionException
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import java.util.concurrent.CompletionException

class HttpError(
    statusCode: HttpStatusCode,
    val cause: Throwable? = null,
    val type: String? = null,
    val title: String = cause?.message ?: statusCode.description,
    val detail: String? = null,
) {
    val statusCode: Int = statusCode.value
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
    exception<BadRequestException> { e ->
        call.respond(HttpStatusCode.BadRequest, e.httpError)
    }
    exception<ParameterConversionException> { e ->
        val parent = e.cause
        if (parent is BadRequestException) {
            call.respond(HttpStatusCode.BadRequest, parent.httpError)
        } else {
            throw e
        }
    }
}
