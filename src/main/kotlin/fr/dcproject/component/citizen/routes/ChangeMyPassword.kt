package fr.dcproject.component.citizen.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.UserRepository
import fr.dcproject.component.auth.UserWithPassword
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenAccessControl
import fr.dcproject.component.citizen.CitizenRef
import io.ktor.application.call
import io.ktor.auth.UserPasswordCredential
import io.ktor.features.BadRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object ChangeMyPassword {
    @Location("/citizens/{citizen}/password/change")
    class ChangePasswordCitizenRequest(citizen: UUID) {
        val citizen = CitizenRef(citizen)
        data class Input(val oldPassword: String, val newPassword: String)
    }

    fun Route.changeMyPassword(ac: CitizenAccessControl, userRepository: UserRepository) {
        put<ChangePasswordCitizenRequest> {
            ac.assert { canChangePassword(it.citizen, citizenOrNull) }
            val content = call.receiveOrBadRequest<ChangePasswordCitizenRequest.Input>()
            userRepository.findByCredentials(UserPasswordCredential(citizen.user.username, content.oldPassword)) ?: throw BadRequestException("Bad Password")
            userRepository.changePassword(
                UserWithPassword(
                    citizen.user.id,
                    content.newPassword,
                )
            )

            call.respond(HttpStatusCode.Created)
        }
    }
}