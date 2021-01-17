package fr.dcproject.component.workgroup.routes

import fr.dcproject.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.WorkgroupVoter
import fr.dcproject.voter.assert
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

    fun Route.deleteWorkgroup(repo: WorkgroupRepository, voter: WorkgroupVoter) {
        delete<DeleteWorkgroupRequest> {
            repo.findById(it.workgroupId)?.let { workgroup ->
                voter.assert { canDelete(workgroup, citizenOrNull) }
                repo.delete(workgroup)
                call.respond(HttpStatusCode.NoContent)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
