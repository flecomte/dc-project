package fr.dcproject.component.auth.routes

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import fr.dcproject.component.auth.UserRepository
import fr.dcproject.component.auth.jwt.makeToken
import io.ktor.application.call
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object Login {
    @Location("/login")
    class LoginRequest

    fun Route.authLogin(userRepo: UserRepository) {
        post<LoginRequest> {
            try {
                val credentials = call.receive<UserPasswordCredential>()
                userRepo.findByCredentials(credentials)?.let { user ->
                    call.respondText(user.makeToken())
                } ?: call.respond(HttpStatusCode.BadRequest, "Username not exist or password is wrong")
            } catch (e: MismatchedInputException) {
                call.respond(HttpStatusCode.BadRequest, "You must be send name and password to the request")
            }
        }
    }
}
