package fr.dcproject.component.workgroup.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.WorkgroupSimple
import fr.dcproject.component.workgroup.routes.CreateWorkgroup.PostWorkgroupRequest.Input
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
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
            call.receive<Input>().run {
                WorkgroupSimple(
                    id ?: UUID.randomUUID(),
                    name,
                    description,
                    logo,
                    anonymous ?: true,
                    citizen
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
