package fr.dcproject.routes

import fr.dcproject.entity.Citizen
import fr.dcproject.security.voter.CitizenVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.repository.Citizen as CitizenRepository

@KtorExperimentalLocationsAPI
object CitizenPaths {
    @Location("/citizens") class CitizensRequest(page: Int = 1, limit: Int = 50, val sort: String? = null, val direction: RepositoryI.Direction? = null, val search: String? = null) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }
    @Location("/citizens/{citizen}") class CitizenRequest(val citizen: Citizen)
    @Location("/citizens/{citizen}/follows/articles") class CitizenFollowArticleRequest(val citizen: Citizen)
    @Location("/citizens/{citizen}/follows/constitutions") class CitizenFollowConstitutionRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.citizen(repo: CitizenRepository) {
    get<CitizenPaths.CitizensRequest> {
        val citizens = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        assertCan(VIEW, citizens.result)
        call.respond(citizens)
    }

    get<CitizenPaths.CitizenRequest> {
        assertCan(VIEW, it.citizen)

        call.respond(it.citizen)
    }
}