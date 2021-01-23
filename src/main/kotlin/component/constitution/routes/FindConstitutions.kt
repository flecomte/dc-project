package fr.dcproject.component.constitution.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.constitution.ConstitutionAccessControl
import fr.dcproject.component.constitution.ConstitutionRepository
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.dcproject.security.assert
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object FindConstitutions {
    @Location("/constitutions")
    class FindConstitutionsRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null
    ) : PaginatedRequestI by PaginatedRequest(page, limit)

    fun Route.findConstitutions(repo: ConstitutionRepository, ac: ConstitutionAccessControl) {
        get<FindConstitutionsRequest> {
            val constitutions = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
            ac.assert { canView(constitutions.result, citizenOrNull) }
            call.respond(constitutions)
        }
    }
}
