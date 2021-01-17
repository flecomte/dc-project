package fr.dcproject.component.citizen.routes

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import fr.dcproject.component.auth.UserRepository
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI

@Location("/citizens/{citizen}/password/change")
class ChangePasswordCitizenRequest(val citizen: Citizen) {
    data class Input(val oldPassword: String, val newPassword: String)
}

@KtorExperimentalLocationsAPI
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