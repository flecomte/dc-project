package fr.dcproject.component.workgroup.database

import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI.Member
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.Parameter
import fr.postgresjson.repository.RepositoryI
import fr.postgresjson.repository.RepositoryI.Direction
import fr.postgresjson.serializer.serialize
import net.pearx.kasechange.toSnakeCase
import java.util.UUID
import fr.dcproject.component.workgroup.database.WorkgroupForView as WorkgroupEntity

class WorkgroupRepository(override var requester: Requester) : RepositoryI {
    fun findById(id: UUID): WorkgroupEntity<CitizenCreator>? {
        val function = requester.getFunction("find_workgroup_by_id")
        return function.selectOne("id" to id)
    }

    fun find(
        page: Int = 1,
        limit: Int = 50,
        sort: String? = null,
        direction: Direction? = null,
        search: String? = null,
        filter: Filter = Filter()
    ): Paginated<WorkgroupEntity<CitizenCreator>> {
        return requester
            .getFunction("find_workgroups")
            .select(
                page,
                limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "search" to search,
                "filter" to filter
            )
    }

    fun <C : CitizenI, W : WorkgroupForUpdateI<C>> upsert(workgroup: W): WorkgroupEntity<CitizenCreator> = requester
        .getFunction("upsert_workgroup")
        .selectOne("resource" to workgroup) ?: error("query 'upsert_workgroup' return null")

    fun <W : WorkgroupRef> delete(workgroup: W) = requester
        .getFunction("delete_workgroup")
        .perform("id" to workgroup.id)

    fun addMember(workgroup: WorkgroupI, member: Member<CitizenI>): Member<CitizenRef>? =
        addMember(workgroup, member.citizen, member.roles)

    fun addMember(workgroup: WorkgroupI, citizen: CitizenI, roles: List<Member.Role>): Member<CitizenRef>? = requester
        .getFunction("add_workgroup_member")
        .selectOne(
            "id" to workgroup.id,
            "members" to Member(citizen, roles).serialize()
        )

    fun <Z : CitizenI> addMembers(workgroup: WorkgroupI, members: List<Member<Z>>): List<Member<CitizenCreator>> = requester
        .getFunction("add_workgroup_members")
        .select(
            "id" to workgroup.id,
            "members" to members.serialize()
        )

    fun <Z : CitizenI> removeMember(workgroup: WorkgroupI, memberToDelete: Member<Z>): List<Member<CitizenCreator>> =
        removeMembers(workgroup, listOf(memberToDelete))

    fun <Z : CitizenI> removeMembers(workgroup: WorkgroupI, membersToDelete: List<Member<Z>>): List<Member<CitizenCreator>> = requester
        .getFunction("remove_workgroup_members")
        .select(
            "id" to workgroup.id,
            "members" to membersToDelete
        )

    fun <Z : CitizenI> updateMembers(workgroup: WorkgroupI, members: List<Member<Z>>): List<Member<CitizenCreator>> = requester
        .getFunction("update_workgroup_members")
        .select(
            "id" to workgroup.id,
            "members" to members
        )

    class Filter(
        val createdById: String? = null,
        val members: List<UUID>? = null
    ) : Parameter
}
