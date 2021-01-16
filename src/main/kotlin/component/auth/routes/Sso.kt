package fr.dcproject.component.auth.routes

import fr.dcproject.component.auth.SsoManager
import fr.dcproject.component.auth.routes.SsoRequest.Input
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

@KtorExperimentalLocationsAPI
@Location("/sso")
class SsoRequest {
    data class Input(val email: String, val url: String)
}

/**
 * Send an email to the citizen with a link to automatically connect
 */
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Route.authSso(ssoManager: SsoManager) {
    post<SsoRequest> {
        call.receive<Input>().run {
            try {
                ssoManager.sendEmail(email, url)
            } catch (e: SsoManager.EmailNotFound) {
                call.respond(HttpStatusCode.NotFound)
            }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
