package fr.dcproject.component.citizen.routes

import fr.dcproject.common.dto.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenAccessControl
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenRepository
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object FindCitizens {
    @Location("/citizens")
    class CitizensRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null
    ) : PaginatedRequestI by PaginatedRequest(page, limit)

    fun Route.findCitizen(ac: CitizenAccessControl, repo: CitizenRepository) {
        get<CitizensRequest> {
            val citizens = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
            ac.assert { canView(citizens.result, citizenOrNull) }
            call.respond(
                citizens.toOutput { c: CitizenCreator ->
                    object {
                        val id: UUID = c.id
                        val name: Any = object {
                            val firstName: String = c.name.firstName
                            val lastName: String = c.name.lastName
                        }
                        val email: String = c.email
                    }
                }
            )
        }
    }
}
