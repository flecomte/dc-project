package fr.dcproject.component.workgroup.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.toUUID
import fr.dcproject.common.validation.isUuid
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.database.WorkgroupRepository
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
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetWorkgroups {
    @Location("/workgroups")
    class WorkgroupsRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null,
        val createdBy: String? = null,
        members: List<String?>? = null
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        val members: List<UUID>? = members?.toUUID()
        fun validate() = Validation<WorkgroupsRequest> {
            WorkgroupsRequest::page {
                minimum(1)
            }
            WorkgroupsRequest::limit {
                minimum(1)
                maximum(50)
            }
            WorkgroupsRequest::sort ifPresent {
                enum(
                    "name",
                    "createdAt",
                )
            }
            WorkgroupsRequest::createdBy ifPresent {
                isUuid()
            }
        }.validate(this)
    }

    fun Route.getWorkgroups(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        get<WorkgroupsRequest> {
            it.validate().badRequestIfNotValid()

            val workgroups = repo.find(
                it.page,
                it.limit,
                it.sort,
                it.direction,
                it.search,
                WorkgroupRepository.Filter(createdById = it.createdBy, members = it.members)
            )
            ac.assert { canView(workgroups.result, citizenOrNull) }
            call.respond(
                HttpStatusCode.OK,
                workgroups.toOutput { it.toOutputListing() }
            )
        }
    }
}
