package fr.dcproject.component.auth.routes

import fr.dcproject.component.auth.PasswordlessAuth
import fr.dcproject.component.auth.routes.Sso.PasswordlessRequest.Input
import fr.dcproject.utils.receiveOrBadRequest
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object Sso {
    @Location("/auth/passwordless")
    class PasswordlessRequest {
        data class Input(val email: String, val url: String)
    }

    /**
     * Send an email to the citizen with a link to automatically connect
     */
    fun Route.authPasswordless(passwordlessAuth: PasswordlessAuth) {
        post<PasswordlessRequest> {
            call.receiveOrBadRequest<Input>().run {
                try {
                    passwordlessAuth.sendEmail(email, url)
                } catch (e: PasswordlessAuth.EmailNotFound) {
                    call.respond(HttpStatusCode.NotFound)
                }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
