package fr.dcproject.component.constitution.routes

import fr.dcproject.common.dto.toOutput
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.constitution.ConstitutionAccessControl
import fr.dcproject.component.constitution.database.ConstitutionRepository
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.joda.time.DateTime
import java.util.UUID

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
            call.respond(
                HttpStatusCode.OK,
                constitutions.toOutput { c ->
                    object {
                        val id: UUID = c.id
                        val title: String = c.title
                        val versionId: UUID = c.versionId
                        val createdAt: DateTime = c.createdAt
                        val createdBy: Any = c.createdBy.toOutput()
                    }
                }
            )
        }
    }
}
