package fr.dcproject.component.workgroup.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.call
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
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
        val members: List<UUID>? = members?.toUUID()
    }

    fun Route.getWorkgroups(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        get<WorkgroupsRequest> {
            val workgroups =
                repo.find(
                    it.page,
                    it.limit,
                    it.sort,
                    it.direction,
                    it.search,
                    WorkgroupRepository.Filter(createdById = it.createdBy, members = it.members)
                )
            ac.assert { canView(workgroups.result, citizenOrNull) }
            call.respond(workgroups)
        }
    }
}
