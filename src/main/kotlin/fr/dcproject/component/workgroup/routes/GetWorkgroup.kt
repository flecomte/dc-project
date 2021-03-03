package fr.dcproject.component.workgroup.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.database.WorkgroupRef
import fr.dcproject.component.workgroup.database.WorkgroupRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetWorkgroup {
    @Location("/workgroups/{workgroup}")
    class WorkgroupRequest(workgroup: UUID) {
        val workgroup = WorkgroupRef(workgroup)
    }

    fun Route.getWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        get<WorkgroupRequest> {
            repo.findById(it.workgroup.id)?.let { workgroup ->
                ac.assert { canView(workgroup, citizenOrNull) }
                call.respond(workgroup)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
