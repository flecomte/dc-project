package fr.dcproject.routes

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import fr.dcproject.JwtConfig
import io.ktor.application.call
import io.ktor.auth.UserPasswordCredential
import io.ktor.features.BadRequestException
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.User as UserRepository

@KtorExperimentalLocationsAPI
object AuthPaths {
    @Location("/login") class LoginRequest
    @Location("/register") class RegisterRequest
}

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Route.auth(userRepo: UserRepository, citizenRepo: CitizenRepository) {
    post <AuthPaths.LoginRequest> {
        try {
            val credentials = call.receive<UserPasswordCredential>()
            val user = userRepo.findByCredentials(credentials) ?: throw BadRequestException("Username not exist or password is wrong")
            call.respondText(JwtConfig.makeToken(user))
        } catch (e: MismatchedInputException) {
            throw BadRequestException("You must be send name and password to the request")
        }
    }

    post <AuthPaths.RegisterRequest> {
        val citizen = call.receive<CitizenEntity>()
        val created = citizenRepo.insertWithUser(citizen)?.user ?: throw BadRequestException("Bad request")

        call.respondText(JwtConfig.makeToken(created))
    }
}
