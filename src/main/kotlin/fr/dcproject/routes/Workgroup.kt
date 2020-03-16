package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.CitizenRef
import fr.dcproject.entity.WorkgroupSimple
import fr.dcproject.entity.request.RequestBuilder
import fr.dcproject.entity.request.getContent
import fr.dcproject.repository.Workgroup.Filter
import fr.dcproject.security.voter.WorkgroupVoter.Action.VIEW
import fr.dcproject.security.voter.WorkgroupVoter.Action.CREATE
import fr.dcproject.security.voter.WorkgroupVoter.Action.UPDATE
import fr.dcproject.security.voter.assertCan
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
import org.koin.core.KoinComponent
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
    class PostWorkgroupRequest : RequestBuilder<WorkgroupSimple<CitizenRef>> {
        class Content(
            val id: UUID?,
            val name: String,
            val description: String,
            val logo: String?,
            val anonymous: Boolean?,
            val owner: CitizenRef?
        ) : KoinComponent {
            fun create(creator: CitizenRef): WorkgroupSimple<CitizenRef> {
                return WorkgroupSimple(
                    id ?: UUID.randomUUID(),
                    name,
                    description,
                    logo,
                    anonymous ?: true,
                    owner ?: creator,
                    creator
                )
            }
        }

        override suspend fun getContent(call: ApplicationCall): WorkgroupSimple<CitizenRef> {
            return call.receive<Content>().create(call.citizen)
        }
    }
}

@KtorExperimentalLocationsAPI
object WorkgroupsMembersPaths {
    @Location("/workgroups/members/{workgroup}")
    class WorkgroupsMembersRequest(val workgroup: WorkgroupEntity) : RequestBuilder<List<CitizenRef>> {
        class Content : MutableList<Content.Item> by mutableListOf() {
            class Item(val id: String)
        }

        override suspend fun getContent(call: ApplicationCall): List<CitizenRef> {
            return call.receive<Content>().map {
                CitizenRef(it.id.toUUID())
            }
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
        call.getContent(it)
            .let { workgroup ->
                assertCan(CREATE, workgroup)
                repo.upsert(workgroup)
            }.let {
                call.respond(HttpStatusCode.Created, it)
            }
    }

    /* Add members to workgroup */
    post<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        call.getContent(it)
            .let { members ->
                assertCan(UPDATE, it.workgroup)
                repo.addMembers(it.workgroup, members)
            }.let {
                call.respond(HttpStatusCode.OK, it)
            }
    }

    /* Delete members of workgroup */
    delete<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        call.getContent(it)
            .let { members ->
                assertCan(UPDATE, it.workgroup)
                repo.removeMembers(it.workgroup, members)
            }.let {
                call.respond(HttpStatusCode.OK, it)
            }
    }

    /* Update members of workgroup */
    put<WorkgroupsMembersPaths.WorkgroupsMembersRequest> {
        call.getContent(it)
            .let { members ->
                assertCan(UPDATE, it.workgroup)
                repo.updateMembers(it.workgroup, members)
            }.let {
                call.respond(HttpStatusCode.OK, it)
            }
    }
}