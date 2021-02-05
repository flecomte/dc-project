package fr.dcproject.component.auth.routes

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.auth.jwt.makeToken
import fr.dcproject.component.auth.routes.Register.RegisterRequest.Input
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.utils.receiveOrBadRequest
import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import org.joda.time.DateTime

@KtorExperimentalLocationsAPI
object Register {
    @Location("/register")
    class RegisterRequest {
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
                val citizen = call.receiveOrBadRequest<Input>().toCitizen()
                val createdCitizen = citizenRepo.insertWithUser(citizen)?.user ?: throw BadRequestException("Bad request")
                call.respondText(createdCitizen.makeToken())
            } catch (e: MissingKotlinParameterException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
