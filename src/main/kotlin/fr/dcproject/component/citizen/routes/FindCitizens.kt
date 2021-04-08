package fr.dcproject.component.citizen.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.CitizenAccessControl
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenRepository
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.postgresjson.repository.RepositoryI
import io.konform.validation.Validation
import io.konform.validation.jsonschema.enum
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minimum
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
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        fun validate() = Validation<CitizensRequest> {
            CitizensRequest::page {
                minimum(1)
            }
            CitizensRequest::limit {
                minimum(1)
                maximum(50)
            }
            CitizensRequest::sort ifPresent {
                enum(
                    "title",
                    "createdAt",
                )
            }
        }.validate(this)
    }

    fun Route.findCitizen(ac: CitizenAccessControl, repo: CitizenRepository) {
        get<CitizensRequest> {
            mustBeAuth()
            it.validate().badRequestIfNotValid()
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
