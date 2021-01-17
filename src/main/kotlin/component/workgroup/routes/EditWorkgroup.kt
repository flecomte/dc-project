package fr.dcproject.component.workgroup.routes

import fr.dcproject.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.routes.EditWorkgroup.PutWorkgroupRequest.Input
import fr.dcproject.component.workgroup.WorkgroupVoter
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.core.KoinComponent
import java.util.*

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

    fun Route.editWorkgroup(repo: WorkgroupRepository, voter: WorkgroupVoter) {
        put<PutWorkgroupRequest> {
            repo.findById(it.workgroupId)?.let { old ->
                call.receive<Input>().run {
                    old.copy(
                        name = name ?: old.name,
                        description = description ?: old.description,
                        logo = logo ?: old.logo,
                        anonymous = anonymous ?: old.anonymous
                    ).let { workgroup ->
                        voter.assert { canUpdate(workgroup, citizenOrNull) }
                        repo.upsert(workgroup)
                        call.respond(HttpStatusCode.OK, it)
                    }
                }
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
