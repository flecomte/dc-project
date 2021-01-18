package fr.dcproject.component.workgroup.routes.members

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.WorkgroupVoter
import fr.dcproject.component.workgroup.WorkgroupWithMembersI
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
    private suspend fun ApplicationCall.getMembersFromRequest(): List<WorkgroupWithMembersI.Member<CitizenRef>> = receive<WorkgroupsMembersRequest.Input>().map {
        WorkgroupWithMembersI.Member(
            citizen = it.citizen,
            roles = it.roles
        )
    }

    @KtorExperimentalLocationsAPI
    fun Route.addMemberToWorkgroup(repo: WorkgroupRepository, voter: WorkgroupVoter) {
        /* Add members to workgroup */
        post<WorkgroupsMembersRequest> {
            repo.findById(it.workgroupId)?.let { workgroup ->
                call.getMembersFromRequest().let { members ->
                    voter.assert { canAddMembers(workgroup, citizenOrNull) }
                    repo.addMembers(workgroup, members)
                }.let { members ->
                    call.respond(HttpStatusCode.Created, members)
                }
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}