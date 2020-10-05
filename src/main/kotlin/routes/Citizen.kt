package fr.dcproject.routes

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import fr.dcproject.citizen
import fr.dcproject.citizenOrNull
import fr.dcproject.entity.Citizen
import fr.dcproject.routes.CitizenPaths.ChangePasswordCitizenRequest
import fr.dcproject.routes.CitizenPaths.CitizenRequest
import fr.dcproject.routes.CitizenPaths.CitizensRequest
import fr.dcproject.routes.CitizenPaths.CurrentCitizenRequest
import fr.dcproject.security.voter.CitizenVoter.Action.CHANGE_PASSWORD
import fr.dcproject.security.voter.CitizenVoter.Action.VIEW
import fr.ktorVoter.assertCan
import fr.ktorVoter.assertCanAll
import fr.postgresjson.repository.RepositoryI.Direction
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.User as UserRepository

@KtorExperimentalLocationsAPI
object CitizenPaths {
    @Location("/citizens")
    class CitizensRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: Direction? = null,
        val search: String? = null
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }

    @Location("/citizens/{citizen}")
    class CitizenRequest(val citizen: Citizen)

    @Location("/citizens/current")
    class CurrentCitizenRequest

    @Location("/citizens/{citizen}/password/change")
    class ChangePasswordCitizenRequest(val citizen: Citizen) {
        data class Content(val oldPassword: String, val newPassword: String)
    }
}

@KtorExperimentalLocationsAPI
fun Route.citizen(
    repo: CitizenRepository,
    userRepository: UserRepository
) {
    get<CitizensRequest> {
        val citizens = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        assertCanAll(VIEW, citizens.result)
        call.respond(citizens)
    }

    get<CitizenRequest> {
        assertCan(VIEW, it.citizen)

        call.respond(it.citizen)
    }

    get<CurrentCitizenRequest> {
        if (citizenOrNull === null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            assertCan(VIEW, citizen)
            call.respond(citizen)
        }
    }

    put<ChangePasswordCitizenRequest> {
        assertCan(CHANGE_PASSWORD, it.citizen)
        try {
            val content = call.receive<ChangePasswordCitizenRequest.Content>()
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