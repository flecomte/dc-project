package fr.dcproject.component.auth.routes

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import fr.dcproject.component.auth.UserRepository
import fr.dcproject.makeToken
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

@KtorExperimentalLocationsAPI
@Location("/login")
class LoginRequest

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
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
