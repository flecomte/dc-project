package fr.dcproject.component.workgroup.routes

import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.security.voter.WorkgroupVoter
import fr.ktorVoter.assertCan
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

@KtorExperimentalLocationsAPI
object GetWorkgroup {
    @Location("/workgroups/{workgroupId}")
    class WorkgroupRequest(val workgroupId: UUID)

    fun Route.getWorkgroup(repo: WorkgroupRepository) {
        get<WorkgroupRequest> {
            repo.findById(it.workgroupId)?.let { workgroup ->
                assertCan(WorkgroupVoter.Action.VIEW, workgroup)
                call.respond(workgroup)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
