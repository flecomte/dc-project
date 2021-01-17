package fr.dcproject.component.workgroup.routes

import fr.dcproject.citizen
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.WorkgroupSimple
import fr.dcproject.component.workgroup.routes.CreateWorkgroup.PostWorkgroupRequest.Input
import fr.dcproject.security.voter.WorkgroupVoter
import fr.ktorVoter.assertCan
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

    fun Route.createWorkgroup(repo: WorkgroupRepository) {
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
                assertCan(WorkgroupVoter.Action.CREATE, workgroup)
                repo.upsert(workgroup)
            }.let {
                call.respond(HttpStatusCode.Created, it)
            }
        }
    }
}
