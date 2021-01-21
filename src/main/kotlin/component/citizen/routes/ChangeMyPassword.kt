package fr.dcproject.component.citizen.routes

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import fr.dcproject.component.auth.UserRepository
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object ChangeMyPassword {
    @Location("/citizens/{citizen}/password/change")
    class ChangePasswordCitizenRequest(val citizen: Citizen) {
        data class Input(val oldPassword: String, val newPassword: String)
    }

    fun Route.changeMyPassword(voter: CitizenVoter, userRepository: UserRepository) {
        put<ChangePasswordCitizenRequest> {
            voter.assert { canChangePassword(it.citizen, citizenOrNull) }
            try {
                val content = call.receive<ChangePasswordCitizenRequest.Input>()
                val currentUser = userRepository.findByCredentials(UserPasswordCredential(citizen.user.username, content.oldPassword))
                val user = it.citizen.user
                if (currentUser == null || currentUser.id != user.id) {
                    call.respond(HttpStatusCode.BadRequest, "Bad password")
                } else {
                    user.plainPassword = content.newPassword
                    userRepository.changePassword(user)

                    call.respond(HttpStatusCode.Created)
                }
            } catch (e: MissingKotlinParameterException) {
                call.respond(HttpStatusCode.BadRequest, "Request format is not correct")
            }
        }
    }
}
