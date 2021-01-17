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
object GetWorkgroup {
    @Location("/workgroups/{workgroupId}")
    class WorkgroupRequest(val workgroupId: UUID)

    fun Route.getWorkgroup(repo: WorkgroupRepository, voter: WorkgroupVoter) {
        get<WorkgroupRequest> {
            repo.findById(it.workgroupId)?.let { workgroup ->
                voter.assert { canView(workgroup, citizenOrNull) }
                call.respond(workgroup)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
