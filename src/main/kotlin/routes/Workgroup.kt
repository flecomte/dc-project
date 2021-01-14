package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.entity.WorkgroupSimple
import fr.dcproject.entity.WorkgroupWithMembersI.Member
import fr.dcproject.entity.WorkgroupWithMembersI.Member.Role
import fr.dcproject.repository.Workgroup.Filter
import fr.dcproject.routes.WorkgroupsPaths.PutWorkgroupRequest.Input
import fr.dcproject.security.voter.WorkgroupVoter.Action.*
import fr.dcproject.security.voter.WorkgroupVoter.Action.UPDATE
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
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import fr.dcproject.repository.Workgroup as WorkgroupRepo
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

    @Location("/workgroups/{workgroupId}")
    class WorkgroupRequest(private val workgroupId: UUID) : KoinComponent {
        val repo: WorkgroupRepo by inject()
        val workgroup = repo.findById(workgroupId) ?: TODO()
    }

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

    @Location("/workgroups/{workgroupId}")
    class PutWorkgroupRequest(val workgroupId: UUID) : KoinComponent {
        class Input(
            val name: String?,
            val description: String?,
            val logo: String?,
            val anonymous: Boolean?
        )
    }

    @Location("/workgroups/{workgroupId}")
    class DeleteWorkgroupRequest(val workgroupId: UUID) : KoinComponent {
        val repo: WorkgroupRepo by inject()
        val workgroup = repo.findById(workgroupId)
    }
}

@KtorExperimentalLocationsAPI
object WorkgroupsMembersPaths {
    @Location("/workgroups/{workgroupId}/members")
    class WorkgroupsMembersRequest(val workgroupId: UUID) : KoinComponent {
        val repo: WorkgroupRepo by inject()
        val workgroup = repo.findById(workgroupId)

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
fun Route.workgroup(repo: WorkgroupRepo) {
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
        repo.findById(it.workgroupId)?.let { old ->
            call.receive<Input>().run {
                old.copy(
                    name = name ?: old.name,
                    description = description ?: old.description,
                    logo = logo ?: old.logo,
                    anonymous = anonymous ?: old.anonymous
                ).let { workgroup ->
                    assertCan(UPDATE, workgroup)
                    repo.upsert(workgroup)
                    call.respond(HttpStatusCode.OK, it)
                }
            }
        } ?: call.respond(HttpStatusCode.NotFound)
    }

    delete<WorkgroupsPaths.DeleteWorkgroupRequest> {
        if (it.workgroup != null) {
            assertCan(DELETE, it.workgroup)
            repo.delete(it.workgroup)
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    /* Add members to workgroup */
    post<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        if (it.workgroup != null) {
            it.getMembers(call)
                .let { members ->
                    assertCan(ADD_MEMBERS, it.workgroup)
                    repo.addMembers(it.workgroup, members)
                }.let { members ->
                    call.respond(HttpStatusCode.Created, members)
                }
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    /* Delete members of workgroup */
    delete<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        if (it.workgroup != null) {
            it.getMembers(call)
                .let { members ->
                    assertCan(REMOVE_MEMBERS, it.workgroup)
                    repo.removeMembers(it.workgroup, members)
                }.let { members ->
                    call.respond(HttpStatusCode.OK, members)
                }
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    /* Update members of workgroup */
    put<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        if (it.workgroup != null) {
            it.getMembers(call)
                .let { members ->
                    assertCan(UPDATE_MEMBERS, it.workgroup)
                    repo.updateMembers(it.workgroup, members)
                }.let { members ->
                    call.respond(HttpStatusCode.OK, members)
                }
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
