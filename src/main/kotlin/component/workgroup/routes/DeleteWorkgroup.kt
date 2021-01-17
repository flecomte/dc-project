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
object DeleteWorkgroup {
    @Location("/workgroups/{workgroupId}")
    class DeleteWorkgroupRequest(val workgroupId: UUID)

    fun Route.deleteWorkgroup(repo: WorkgroupRepository) {
        delete<DeleteWorkgroupRequest> {
            repo.findById(it.workgroupId)?.let { workgroup ->
                assertCan(WorkgroupVoter.Action.DELETE, workgroup)
                repo.delete(workgroup)
                call.respond(HttpStatusCode.NoContent)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
