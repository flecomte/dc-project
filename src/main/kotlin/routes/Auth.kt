package fr.dcproject.routes

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import fr.dcproject.JwtConfig
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.entity.UserI.Roles.ROLE_USER
import fr.dcproject.messages.SsoManager
import fr.dcproject.routes.AuthPaths.LoginRequest
import fr.dcproject.routes.AuthPaths.RegisterRequest
import fr.dcproject.routes.AuthPaths.SsoRequest
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import fr.dcproject.component.citizen.Citizen as CitizenEntity
import fr.dcproject.repository.User as UserRepository

@KtorExperimentalLocationsAPI
object AuthPaths {
    @Location("/login")
    class LoginRequest

    @Location("/register")
    class RegisterRequest

    @Location("/sso")
    class SsoRequest {
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
    post<LoginRequest> {
        try {
            val credentials = call.receive<UserPasswordCredential>()
            val user = userRepo.findByCredentials(credentials) ?: throw WrongLoginOrPassword()
            call.respondText(JwtConfig.makeToken(user))
        } catch (e: MismatchedInputException) {
            call.respond(HttpStatusCode.BadRequest, "You must be send name and password to the request")
        } catch (e: WrongLoginOrPassword) {
            call.respond(HttpStatusCode.BadRequest, e.message)
        }
    }

    post<RegisterRequest> {
        try {
            val citizen = call.receive<CitizenEntity>()
            citizen.user.roles = listOf(ROLE_USER)
            val created = citizenRepo.insertWithUser(citizen)?.user ?: throw BadRequestException("Bad request")
            call.respondText(JwtConfig.makeToken(created))
        } catch (e: MissingKotlinParameterException) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    post<SsoRequest> {
        val content = call.receive<SsoRequest.Content>()
        try {
            ssoManager.sendEmail(content.email, content.url)
        } catch (e: SsoManager.EmailNotFound) {
            call.respond(HttpStatusCode.NotFound)
        }

        call.respond(HttpStatusCode.NoContent)
    }
}

class WrongLoginOrPassword(override val message: String = "Username not exist or password is wrong") : Exception()
