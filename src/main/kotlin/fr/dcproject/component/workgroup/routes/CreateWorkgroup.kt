package fr.dcproject.component.workgroup.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.database.WorkgroupForUpdate
import fr.dcproject.component.workgroup.database.WorkgroupRepository
import fr.dcproject.component.workgroup.routes.CreateWorkgroup.PostWorkgroupRequest.Input
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object CreateWorkgroup {
    @Location("/workgroups")
    open class PostWorkgroupRequest {
        class Input(
            val id: UUID?,
            val name: String,
            val description: String,
            val logo: String?,
            val anonymous: Boolean?
        )
    }

    fun Route.createWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        post<PostWorkgroupRequest> {
            call.receiveOrBadRequest<Input>().run {
                WorkgroupForUpdate(
                    id ?: UUID.randomUUID(),
                    name,
                    description,
                    citizen,
                    logo,
                    anonymous ?: true,
                )
            }.let { workgroup ->
                ac.assert { canCreate(workgroup, citizenOrNull) }
                repo.upsert(workgroup)
            }.let {
                call.respond(HttpStatusCode.Created, it)
            }
        }
    }
}
