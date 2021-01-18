package fr.dcproject.component.citizen.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.voter.assert
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
@Location("/citizens")
class CitizensRequest(
    page: Int = 1,
    limit: Int = 50,
    val sort: String? = null,
    val direction: RepositoryI.Direction? = null,
    val search: String? = null
) {
    val page: Int = if (page < 1) 1 else page
    val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
}

@KtorExperimentalLocationsAPI
fun Route.findCitizen(voter: CitizenVoter, repo: CitizenRepository) {
    get<CitizensRequest> {
        val citizens = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        voter.assert { canView(citizens.result, citizenOrNull) }
        call.respond(citizens)
    }
}
