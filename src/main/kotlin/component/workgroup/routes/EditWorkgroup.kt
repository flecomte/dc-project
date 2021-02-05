package fr.dcproject.component.workgroup.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.routes.EditWorkgroup.PutWorkgroupRequest.Input
import fr.dcproject.security.assert
import fr.dcproject.utils.receiveOrBadRequest
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import org.koin.core.KoinComponent
import java.util.UUID

@KtorExperimentalLocationsAPI
object EditWorkgroup {
    @Location("/workgroups/{workgroupId}")
    class PutWorkgroupRequest(val workgroupId: UUID) : KoinComponent {
        class Input(
            val name: String?,
            val description: String?,
            val logo: String?,
            val anonymous: Boolean?
        )
    }

    fun Route.editWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        put<PutWorkgroupRequest> {
            repo.findById(it.workgroupId)?.let { old ->
                call.receiveOrBadRequest<Input>().run {
                    old.copy(
                        name = name ?: old.name,
                        description = description ?: old.description,
                        logo = logo ?: old.logo,
                        anonymous = anonymous ?: old.anonymous
                    ).let { workgroup ->
                        ac.assert { canUpdate(workgroup, citizenOrNull) }
                        repo.upsert(workgroup)
                        call.respond(HttpStatusCode.OK, it)
                    }
                }
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
