package fr.dcproject.component.workgroup.routes.members

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.WorkgroupWithMembersI
import fr.dcproject.security.assert
import fr.dcproject.utils.receiveOrBadRequest
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import org.koin.core.KoinComponent
import java.util.UUID

@KtorExperimentalLocationsAPI
object AddMemberToWorkgroup {
    @Location("/workgroups/{workgroupId}/members")
    class WorkgroupsMembersRequest(val workgroupId: UUID) : KoinComponent {
        class Input : MutableList<Input.Item> by mutableListOf() {
            class Item(val citizen: CitizenRef, roles: List<String> = emptyList()) {
                val roles: List<WorkgroupWithMembersI.Member.Role> = roles.map {
                    WorkgroupWithMembersI.Member.Role.valueOf(it)
                }
            }
        }
    }

    @KtorExperimentalLocationsAPI
    private suspend fun ApplicationCall.getMembersFromRequest(): List<WorkgroupWithMembersI.Member<CitizenRef>> = receiveOrBadRequest<WorkgroupsMembersRequest.Input>().map {
        WorkgroupWithMembersI.Member(
            citizen = it.citizen,
            roles = it.roles
        )
    }

    @KtorExperimentalLocationsAPI
    fun Route.addMemberToWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        /* Add members to workgroup */
        post<WorkgroupsMembersRequest> {
            repo.findById(it.workgroupId)?.let { workgroup ->
                call.getMembersFromRequest().let { members ->
                    ac.assert { canAddMembers(workgroup, citizenOrNull) }
                    repo.addMembers(workgroup, members)
                }.let { members ->
                    call.respond(HttpStatusCode.Created, members)
                }
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
