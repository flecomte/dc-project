package fr.dcproject.component.auth.routes

import fr.dcproject.component.auth.PasswordlessAuth
import fr.dcproject.component.auth.routes.PasswordlessRequest.Input
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

@KtorExperimentalLocationsAPI
@Location("/auth/passwordless")
class PasswordlessRequest {
    data class Input(val email: String, val url: String)
}

/**
 * Send an email to the citizen with a link to automatically connect
 */
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Route.authPasswordless(passwordlessAuth: PasswordlessAuth) {
    post<PasswordlessRequest> {
        call.receive<Input>().run {
            try {
                passwordlessAuth.sendEmail(email, url)
            } catch (e: PasswordlessAuth.EmailNotFound) {
                call.respond(HttpStatusCode.NotFound)
            }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
