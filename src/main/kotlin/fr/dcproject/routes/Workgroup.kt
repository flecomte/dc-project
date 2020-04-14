package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.CitizenRef
import fr.dcproject.entity.WorkgroupSimple
import fr.dcproject.repository.Workgroup.Filter
import fr.dcproject.security.voter.WorkgroupVoter.Action.VIEW
import fr.dcproject.security.voter.WorkgroupVoter.Action.CREATE
import fr.dcproject.security.voter.WorkgroupVoter.Action.UPDATE
import fr.dcproject.security.voter.WorkgroupVoter.ActionMembers.ADD as ADD_MEMBERS
import fr.dcproject.security.voter.WorkgroupVoter.ActionMembers.UPDATE as UPDATE_MEMBERS
import fr.dcproject.security.voter.WorkgroupVoter.ActionMembers.REMOVE as REMOVE_MEMBERS
import fr.ktorVoter.assertCan
import fr.dcproject.utils.toUUID
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.locations.put
import io.ktor.locations.delete
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.*
import fr.dcproject.entity.Workgroup as WorkgroupEntity
import fr.dcproject.repository.Workgroup as WorkgroupRepository

@KtorExperimentalLocationsAPI
object WorkgroupsPaths {
    @Location("/workgroups")
    class WorkgroupsRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null,
        val createdBy: String? = null
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
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
            val anonymous: Boolean?,
            val owner: UUID?
        )

        suspend fun getNewWorkgroup(call: ApplicationCall): WorkgroupSimple<CitizenRef> = call.receive<Body>().run {
            WorkgroupSimple(
                id ?: UUID.randomUUID(),
                name,
                description,
                logo,
                anonymous ?: true,
                owner?.let { CitizenRef(it) } ?: call.citizen,
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
            val anonymous: Boolean?,
            val owner: UUID?
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
            class Item(id: String) {
                val id = id.toUUID()
            }
        }

        suspend fun getMembers(call: ApplicationCall): List<CitizenRef> = call.receive<Body>().map {
            CitizenRef(it.id)
        }
    }
}

@KtorExperimentalLocationsAPI
fun Route.workgroup(repo: WorkgroupRepository) {
    get<WorkgroupsPaths.WorkgroupsRequest> {
        val workgroups =
            repo.find(it.page, it.limit, it.sort, it.direction, it.search, Filter(createdById = it.createdBy))
        assertCan(VIEW, workgroups.result)
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
            }.let {
                call.respond(HttpStatusCode.Created, it)
            }
    }

    /* Delete members of workgroup */
    delete<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        it.getMembers(call)
            .let { members ->
                assertCan(REMOVE_MEMBERS, it.workgroup)
                repo.removeMembers(it.workgroup, members)
            }.let {
                call.respond(HttpStatusCode.OK, it)
            }
    }

    /* Update members of workgroup */
    put<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        it.getMembers(call)
            .let { members ->
                assertCan(UPDATE_MEMBERS, it.workgroup)
                repo.updateMembers(it.workgroup, members)
            }.let {
                call.respond(HttpStatusCode.OK, it)
            }
    }
}
