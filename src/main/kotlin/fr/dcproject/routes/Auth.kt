package fr.dcproject.routes

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import fr.dcproject.JwtConfig
import fr.dcproject.entity.User
import fr.dcproject.messages.SsoManager
import fr.dcproject.routes.AuthPaths.LoginRequest
import fr.dcproject.routes.AuthPaths.RegisterRequest
import fr.dcproject.routes.AuthPaths.SsoRequest
import io.ktor.application.call
import io.ktor.auth.UserPasswordCredential
import io.ktor.features.BadRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
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
    @Location("/sso") class SsoRequest {
        data class Content(val email: String, val url: String)
    }
}

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Route.auth(
    userRepo: UserRepository,
    citizenRepo: CitizenRepository,
    ssoManager: SsoManager
) {
    post <LoginRequest> {
        try {
            val credentials = call.receive<UserPasswordCredential>()
            val user = userRepo.findByCredentials(credentials) ?: throw BadRequestException("Username not exist or password is wrong")
            call.respondText(JwtConfig.makeToken(user))
        } catch (e: MismatchedInputException) {
            throw BadRequestException("You must be send name and password to the request")
        }
    }

    post <RegisterRequest> {
        val citizen = call.receive<CitizenEntity>()
        citizen.user?.roles = listOf(User.Roles.ROLE_USER)
        val created = citizenRepo.insertWithUser(citizen)?.user ?: throw BadRequestException("Bad request")

        call.respondText(JwtConfig.makeToken(created))
    }

    post<SsoRequest> {
        val content = call.receive<SsoRequest.Content>()
        ssoManager.sendMail(content.email, content.url)

        call.respond(HttpStatusCode.NoContent)
    }
}
