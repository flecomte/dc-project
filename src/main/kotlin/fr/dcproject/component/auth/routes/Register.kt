package fr.dcproject.component.auth.routes

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.common.validation.email
import fr.dcproject.common.validation.passwordScore
import fr.dcproject.component.auth.database.UserForCreate
import fr.dcproject.component.auth.database.UserI
import fr.dcproject.component.auth.jwt.makeToken
import fr.dcproject.component.auth.routes.Register.RegisterRequest.Input
import fr.dcproject.component.citizen.database.CitizenForCreate
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRepository
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.accept
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
                val password: String
            )

            fun validate() = Validation<Input> {
                Input::name {
                    Name::firstName {
                        minLength(2)
                        maxLength(50)
                    }
                    Name::lastName {
                        minLength(2)
                        maxLength(50)
                    }
                    Name::civility ifPresent {
                        minLength(1)
                        maxLength(10)
                    }
                }
                Input::user {
                    User::username {
                        minLength(7)
                        maxLength(30)
                    }
                    User::password {
                        passwordScore(15)
                    }
                }
                Input::email {
                    email()
                }
            }.validate(this)
        }
    }

    fun Route.authRegister(citizenRepo: CitizenRepository) {
        fun Input.toCitizen(): CitizenForCreate = CitizenForCreate(
            name = CitizenI.Name(name.firstName, name.lastName, name.civility),
            birthday = birthday,
            email = email,
            followAnonymous = followAnonymous,
            voteAnonymous = voteAnonymous,
            user = UserForCreate(
                username = user.username,
                password = user.password,
                roles = listOf(UserI.Roles.ROLE_USER)
            )
        )

        post<RegisterRequest> {
            try {
                val citizen = call.receiveOrBadRequest<Input>()
                    .apply { validate().badRequestIfNotValid() }
                    .toCitizen()

                citizenRepo.insertWithUser(citizen)?.user?.makeToken()?.let { token ->
                    if (call.request.accept() == ContentType.Application.Json.toString()) {
                        call.respond(
                            object {
                                val token: String = token
                            }
                        )
                    } else {
                        call.respondText(token)
                    }
                } ?: throw BadRequestException("Bad request")
            } catch (e: MissingKotlinParameterException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
