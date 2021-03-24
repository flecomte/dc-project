package fr.dcproject.component.workgroup.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.database.WorkgroupRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object DeleteWorkgroup {
    @Location("/workgroups/{workgroupId}")
    class DeleteWorkgroupRequest(val workgroupId: UUID)

    fun Route.deleteWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        delete<DeleteWorkgroupRequest> {
            repo.findById(it.workgroupId)?.let { workgroup ->
                ac.assert { canDelete(workgroup, citizenOrNull) }
                repo.delete(workgroup)
                call.respond(HttpStatusCode.NoContent)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
