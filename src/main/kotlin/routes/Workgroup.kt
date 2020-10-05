package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.CitizenRef
import fr.dcproject.entity.WorkgroupSimple
import fr.dcproject.entity.WorkgroupWithMembersI.Member
import fr.dcproject.entity.WorkgroupWithMembersI.Member.Role
import fr.dcproject.repository.Workgroup.Filter
import fr.dcproject.security.voter.WorkgroupVoter.Action.CREATE
import fr.dcproject.security.voter.WorkgroupVoter.Action.UPDATE
import fr.dcproject.security.voter.WorkgroupVoter.Action.VIEW
import fr.dcproject.utils.toUUID
import fr.ktorVoter.assertCan
import fr.ktorVoter.assertCanAll
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*
import fr.dcproject.entity.Workgroup as WorkgroupEntity
import fr.dcproject.repository.Workgroup as WorkgroupRepository
import fr.dcproject.security.voter.WorkgroupVoter.ActionMembers.ADD as ADD_MEMBERS
import fr.dcproject.security.voter.WorkgroupVoter.ActionMembers.REMOVE as REMOVE_MEMBERS
import fr.dcproject.security.voter.WorkgroupVoter.ActionMembers.UPDATE as UPDATE_MEMBERS

@KtorExperimentalLocationsAPI
object WorkgroupsPaths {
    @Location("/workgroups")
    class WorkgroupsRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null,
        val createdBy: String? = null,
        members: List<String?>? = null
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
        val members: List<UUID>? = members?.toUUID()
    }

    @Location("/workgroups/{workgroup}")
    class WorkgroupRequest(val workgroup: WorkgroupEntity)

    @Location("/workgroups")
    open class PostWorkgroupRequest {
        class Body(
            val id: UUID?,
            val name: String,
            val description: String,
            val logo: String?,
            val anonymous: Boolean?
        )

        suspend fun getNewWorkgroup(call: ApplicationCall): WorkgroupSimple<CitizenRef> = call.receive<Body>().run {
            WorkgroupSimple(
                id ?: UUID.randomUUID(),
                name,
                description,
                logo,
                anonymous ?: true,
                call.citizen
            )
        }
    }

    @Location("/workgroups/{workgroup}")
    class PutWorkgroupRequest(val workgroup: WorkgroupEntity) {
        class Body(
            val name: String?,
            val description: String?,
            val logo: String?,
            val anonymous: Boolean?
        )

        suspend fun updateWorkgroup(call: ApplicationCall): Unit = call.receive<Body>().run {
            name?.let { workgroup.name = it }
            description?.let { workgroup.description = it }
            logo?.let { workgroup.logo = it }
            anonymous?.let { workgroup.anonymous = it }
        }
    }

    @Location("/workgroups/{workgroup}")
    class DeleteWorkgroupRequest(val workgroup: WorkgroupEntity)
}

@KtorExperimentalLocationsAPI
object WorkgroupsMembersPaths {
    @Location("/workgroups/{workgroup}/members")
    class WorkgroupsMembersRequest(val workgroup: WorkgroupEntity) {
        class Body : MutableList<Body.Item> by mutableListOf() {
            class Item(val citizen: CitizenRef, roles: List<String> = emptyList()) {
                val roles: List<Role> = roles.map {
                    Role.valueOf(it)
                }
            }
        }

        suspend fun getMembers(call: ApplicationCall): List<Member<CitizenRef>> = call.receive<Body>().map {
            Member(
                citizen = it.citizen,
                roles = it.roles
            )
        }
    }
}

@KtorExperimentalLocationsAPI
fun Route.workgroup(repo: WorkgroupRepository) {
    get<WorkgroupsPaths.WorkgroupsRequest> {
        val workgroups =
            repo.find(it.page, it.limit, it.sort, it.direction, it.search, Filter(createdById = it.createdBy, members = it.members))
        assertCanAll(VIEW, workgroups.result)
        call.respond(workgroups)
    }

    get<WorkgroupsPaths.WorkgroupRequest> {
        assertCan(VIEW, it.workgroup)

        call.respond(it.workgroup)
    }

    post<WorkgroupsPaths.PostWorkgroupRequest> {
        it.getNewWorkgroup(call)
            .let { workgroup ->
                assertCan(CREATE, workgroup)
                repo.upsert(workgroup)
            }.let {
                call.respond(HttpStatusCode.Created, it)
            }
    }

    put<WorkgroupsPaths.PutWorkgroupRequest> {
        it.updateWorkgroup(call).let { workgroup ->
            assertCan(UPDATE, workgroup)
            repo.upsert(workgroup as WorkgroupSimple<CitizenRef>)
        }.let {
            call.respond(HttpStatusCode.OK, it)
        }
    }

    delete<WorkgroupsPaths.DeleteWorkgroupRequest> {
        assertCan(UPDATE, it.workgroup)
        repo.delete(it.workgroup)
        call.respond(HttpStatusCode.NoContent, it)
    }

    /* Add members to workgroup */
    post<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        it.getMembers(call)
            .let { members ->
                assertCan(ADD_MEMBERS, it.workgroup)
                repo.addMembers(it.workgroup, members)
            }.let { members ->
                call.respond(HttpStatusCode.Created, members)
            }
    }

    /* Delete members of workgroup */
    delete<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        it.getMembers(call)
            .let { members ->
                assertCan(REMOVE_MEMBERS, it.workgroup)
                repo.removeMembers(it.workgroup, members)
            }.let { members ->
                call.respond(HttpStatusCode.OK, members)
            }
    }

    /* Update members of workgroup */
    put<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        it.getMembers(call)
            .let { members ->
                assertCan(UPDATE_MEMBERS, it.workgroup)
                repo.updateMembers(it.workgroup, members)
            }.let { members ->
                call.respond(HttpStatusCode.OK, members)
            }
    }
}
