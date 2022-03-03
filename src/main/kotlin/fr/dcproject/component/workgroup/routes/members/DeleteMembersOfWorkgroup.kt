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
import io.ktor.locations.delete
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID
import fr.dcproject.component.workgroup.routes.members.DeleteMembersOfWorkgroup.WorkgroupsMembersRequest.Input as Input

@KtorExperimentalLocationsAPI
object DeleteMembersOfWorkgroup {
    @Location("/workgroups/{workgroupId}/members")
    class WorkgroupsMembersRequest(val workgroupId: UUID) {
        class Input : MutableList<Input.Member> by mutableListOf() {
            class Member(val citizen: CitizenRef)
        }
    }

    private suspend fun ApplicationCall.getMembersFromRequest(): List<WorkgroupWithMembersI.Member<CitizenRef>> =
        receiveOrBadRequest<Input>().map { WorkgroupWithMembersI.Member(it.citizen) }

    fun Route.deleteMemberOfWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        /* Delete members of workgroup */
        delete<WorkgroupsMembersRequest> {
            mustBeAuth()
            repo.findById(it.workgroupId)?.let { workgroup ->
                call.getMembersFromRequest()
                    .let { members ->
                        ac.canRemoveMembers(workgroup, citizenOrNull).assert()
                        repo.removeMembers(workgroup, members)
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
