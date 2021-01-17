package fr.dcproject.component.workgroup.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.WorkgroupSimple
import fr.dcproject.component.workgroup.WorkgroupVoter
import fr.dcproject.component.workgroup.routes.CreateWorkgroup.PostWorkgroupRequest.Input
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

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

    fun Route.createWorkgroup(repo: WorkgroupRepository, voter: WorkgroupVoter) {
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
                voter.assert { canCreate(workgroup, citizenOrNull) }
                repo.upsert(workgroup)
            }.let {
                call.respond(HttpStatusCode.Created, it)
            }
        }
    }
}
