package fr.dcproject.component.workgroup.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.WorkgroupVoter
import fr.dcproject.utils.toUUID
import fr.dcproject.voter.assert
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

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

    fun Route.getWorkgroups(repo: WorkgroupRepository, voter: WorkgroupVoter) {
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
            voter.assert { canView(workgroups.result, citizenOrNull) }
            call.respond(workgroups)
        }
    }
}
