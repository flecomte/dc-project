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
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import org.koin.core.KoinComponent
import java.util.UUID

@KtorExperimentalLocationsAPI
object UpdateMemberOfWorkgroup {
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

    private suspend fun ApplicationCall.getMembersFromRequest(): List<WorkgroupWithMembersI.Member<CitizenRef>> = receiveOrBadRequest<WorkgroupsMembersRequest.Input>().map {
        WorkgroupWithMembersI.Member(
            citizen = it.citizen,
            roles = it.roles
        )
    }

    fun Route.updateMemberOfWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        /* Update members of workgroup */
        put<WorkgroupsMembersRequest> {
            repo.findById(it.workgroupId)?.let { workgroup ->
                call.getMembersFromRequest().let { members ->
                    ac.assert { canUpdateMembers(workgroup, citizenOrNull) }
                    repo.updateMembers(workgroup, members)
                }.let { members ->
                    call.respond(HttpStatusCode.OK, members)
                }
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
