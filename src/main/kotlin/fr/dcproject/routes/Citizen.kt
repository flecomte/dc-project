package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.Citizen
import fr.dcproject.routes.CitizenPaths.ChangePasswordCitizenRequest
import fr.dcproject.routes.CitizenPaths.CitizenRequest
import fr.dcproject.routes.CitizenPaths.CitizensRequest
import fr.dcproject.routes.CitizenPaths.CurrentCitizenRequest
import fr.dcproject.security.voter.CitizenVoter.Action.CHANGE_PASSWORD
import fr.dcproject.security.voter.CitizenVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import fr.postgresjson.repository.RepositoryI.Direction
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.User as UserRepository

@KtorExperimentalLocationsAPI
object CitizenPaths {
    @Location("/citizens") class CitizensRequest(page: Int = 1, limit: Int = 50, val sort: String? = null, val direction: Direction? = null, val search: String? = null) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }
    @Location("/citizens/{citizen}") class CitizenRequest(val citizen: Citizen)
    @Location("/citizens/current") class CurrentCitizenRequest
    @Location("/citizens/{citizen}/password/change") class ChangePasswordCitizenRequest(val citizen: Citizen) {
        data class Content(val password: String)
    }
}

@KtorExperimentalLocationsAPI
fun Route.citizen(
    repo: CitizenRepository,
    userRepository: UserRepository
) {
    get<CitizensRequest> {
        val citizens = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        assertCan(VIEW, citizens.result)
        call.respond(citizens)
    }

    get<CitizenRequest> {
        assertCan(VIEW, it.citizen)

        call.respond(it.citizen)
    }

    get<CurrentCitizenRequest> {
        assertCan(VIEW, citizen)

        call.respond(citizen)
    }

    put<ChangePasswordCitizenRequest> {
        assertCan(CHANGE_PASSWORD, it.citizen)
        val content = call.receive<ChangePasswordCitizenRequest.Content>()

        val user = it.citizen.user ?: error("Citizen must have User")

        user.plainPassword = content.password
        userRepository.changePassword(user)

        call.respond(HttpStatusCode.Created)
    }
}