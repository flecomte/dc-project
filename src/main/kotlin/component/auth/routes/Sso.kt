package fr.dcproject.component.auth.routes

import fr.dcproject.component.auth.PasswordlessAuth
import fr.dcproject.component.auth.routes.PasswordlessRequest.Input
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI

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
