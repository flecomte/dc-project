package fr.dcproject.component.workgroup.routes.members

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.database.WorkgroupRepository
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI
import fr.dcproject.component.workgroup.routes.toOutput
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object UpdateMemberOfWorkgroup {
    @Location("/workgroups/{workgroupId}/members")
    class WorkgroupsMembersRequest(val workgroupId: UUID) {
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
            mustBeAuth()
            repo.findById(it.workgroupId)?.let { workgroup ->
                call.getMembersFromRequest().let { members ->
                    ac.canUpdateMembers(workgroup, citizenOrNull).assert()
                    repo.updateMembers(workgroup, members)
                }.let { members ->
                    call.respond(
                        HttpStatusCode.OK,
                        members.toOutput()
                    )
                }
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
