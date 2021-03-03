package fr.dcproject.component.workgroup.routes.members

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.database.WorkgroupRepository
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.response.respond
import io.ktor.routing.Route
import org.koin.core.KoinComponent
import java.util.UUID

@KtorExperimentalLocationsAPI
object DeleteMembersOfWorkgroup {
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

    fun Route.deleteMemberOfWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        /* Delete members of workgroup */
        delete<WorkgroupsMembersRequest> {
            repo.findById(it.workgroupId)?.let { workgroup ->
                call.getMembersFromRequest().let { members ->
                    ac.assert { canView(workgroup, citizenOrNull) }
                    repo.removeMembers(workgroup, members)
                }.let { members ->
                    call.respond(HttpStatusCode.OK, members)
                }
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
