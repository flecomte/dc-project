package fr.dcproject.component.constitution.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.constitution.ConstitutionAccessControl
import fr.dcproject.component.constitution.database.ConstitutionRepository
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.postgresjson.repository.RepositoryI
import io.konform.validation.Validation
import io.konform.validation.jsonschema.enum
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minimum
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
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        fun validate() = Validation<FindConstitutionsRequest> {
            FindConstitutionsRequest::page {
                minimum(1)
            }
            FindConstitutionsRequest::limit {
                minimum(1)
                maximum(50)
            }
            FindConstitutionsRequest::sort ifPresent {
                enum(
                    "title",
                    "createdAt",
                )
            }
        }.validate(this)
    }

    fun Route.findConstitutions(repo: ConstitutionRepository, ac: ConstitutionAccessControl) {
        get<FindConstitutionsRequest> {
            it.validate().badRequestIfNotValid()
            val constitutions = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
            ac.canView(constitutions.result, citizenOrNull).assert()
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
