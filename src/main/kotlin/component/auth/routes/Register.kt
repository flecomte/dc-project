package fr.dcproject.component.auth.routes

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import fr.dcproject.JwtConfig
import fr.dcproject.component.auth.routes.RegisterRequest.Input
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import org.joda.time.DateTime

@KtorExperimentalLocationsAPI
@Location("/register")
private class RegisterRequest {
    data class Input(
        val name: Name,
        val email: String,
        val birthday: DateTime,
        val voteAnonymous: Boolean = true,
        val followAnonymous: Boolean = true,
        val user: User
    ) {
        data class Name(
            val firstName: String,
            val lastName: String,
            val civility: String? = null
        )
        data class User(
            val username: String,
            val plainPassword: String? = null
        )
    }
}

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Route.authRegister(citizenRepo: CitizenRepository) {
    fun Input.toCitizen(): Citizen = Citizen(
        name = CitizenI.Name(name.firstName, name.lastName, name.civility),
        birthday = birthday,
        email = email,
        followAnonymous = followAnonymous,
        voteAnonymous = voteAnonymous,
        user = User(
            username = user.username,
            plainPassword = user.plainPassword,
            roles = listOf(UserI.Roles.ROLE_USER)
        )
    )

    post<RegisterRequest> {
        try {
            val citizen = call.receive<Input>().toCitizen()
            val createdCitizen = citizenRepo.insertWithUser(citizen)?.user ?: throw BadRequestException("Bad request")
            call.respondText(JwtConfig.makeToken(createdCitizen))
        } catch (e: MissingKotlinParameterException) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}
