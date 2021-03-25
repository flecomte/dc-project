package fr.dcproject.application.http

import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException
import fr.dcproject.common.security.AccessDeniedException
import fr.dcproject.component.auth.ForbiddenException
import fr.dcproject.component.auth.user
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import java.util.concurrent.CompletionException

fun statusPagesInstallation(): StatusPages.Configuration.() -> Unit = {
    exception<CompletionException> { e ->
        val parent = e.cause?.cause
        if (parent is GenericDatabaseException) {
            call.respond(HttpStatusCode.BadRequest, parent.errorMessage.message!!)
        } else {
            throw e
        }
    }
    exception<NotFoundException> { e ->
        call.respond(HttpStatusCode.NotFound, e.message!!)
    }
    exception<AccessDeniedException> {
        if (call.user == null) call.respond(HttpStatusCode.Unauthorized)
        else call.respond(HttpStatusCode.Forbidden)
    }
    exception<ForbiddenException> {
        call.respond(HttpStatusCode.Forbidden)
    }
}
