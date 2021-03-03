package fr.dcproject.component.auth.routes

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.database.UserRepository
import fr.dcproject.component.auth.jwt.makeToken
import fr.dcproject.component.auth.routes.Login.LoginRequest.Input
import io.ktor.application.call
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object Login {
    @Location("/login")
    class LoginRequest {
        data class Input(
            val username: String,
            val password: String,
        )
    }

    fun Route.authLogin(userRepo: UserRepository) {
        post<LoginRequest> {
            try {
                val credentials = call.receiveOrBadRequest<Input>().run {
                    UserPasswordCredential(username, password)
                }

                userRepo.findByCredentials(credentials)?.let { user ->
                    call.respondText(user.makeToken())
                } ?: call.respond(HttpStatusCode.BadRequest, "Username not exist or password is wrong")
            } catch (e: MismatchedInputException) {
                call.respond(HttpStatusCode.BadRequest, "You must be send name and password to the request")
            }
        }
    }
}
