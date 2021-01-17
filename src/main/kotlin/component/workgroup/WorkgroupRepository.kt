package fr.dcproject.component.workgroup

import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.workgroup.WorkgroupWithMembersI.Member
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.Parameter
import fr.postgresjson.repository.RepositoryI
import fr.postgresjson.repository.RepositoryI.Direction
import fr.postgresjson.serializer.serialize
import net.pearx.kasechange.toSnakeCase
import java.util.*
import fr.dcproject.component.workgroup.Workgroup as WorkgroupEntity

class WorkgroupRepository(override var requester: Requester) : RepositoryI {
    fun findById(id: UUID): WorkgroupEntity<CitizenBasic>? {
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
    ): Paginated<WorkgroupEntity<CitizenBasic>> {
        return requester
            .getFunction("find_workgroups")
            .select(
                page, limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "search" to search,
                "filter" to filter
            )
    }

    fun <C : CitizenI, W : WorkgroupSimple<C>> upsert(workgroup: W): WorkgroupEntity<CitizenBasic> = requester
        .getFunction("upsert_workgroup")
        .selectOne("resource" to workgroup) ?: error("query 'upsert_workgroup' return null")

    fun <W : WorkgroupRef> delete(workgroup: W) = requester
            .getFunction("delete_workgroup")
            .perform("id" to workgroup.id)

    fun addMember(workgroup: WorkgroupI, member: Member<CitizenI>): Member<CitizenBasic>? =
        addMember(workgroup, member.citizen, member.roles)

    fun addMember(workgroup: WorkgroupI, citizen: CitizenI, roles: List<Member.Role>): Member<CitizenBasic>? = requester
        .getFunction("add_workgroup_member")
        .selectOne(
            "id" to workgroup.id,
            "members" to Member(citizen, roles).serialize()
        )

    fun <Z : CitizenI> addMembers(workgroup: WorkgroupI, members: List<Member<Z>>): List<Member<CitizenBasic>> = requester
        .getFunction("add_workgroup_members")
        .select(
            "id" to workgroup.id,
            "members" to members.serialize()
        )

    fun <Z : CitizenI> removeMember(workgroup: WorkgroupI, memberToDelete: Member<Z>): List<Member<CitizenBasic>> =
        removeMembers(workgroup, listOf(memberToDelete))

    fun <Z : CitizenI> removeMembers(workgroup: WorkgroupI, membersToDelete: List<Member<Z>>): List<Member<CitizenBasic>> = requester
        .getFunction("remove_workgroup_members")
        .select(
            "id" to workgroup.id,
            "members" to membersToDelete
        )

    fun <Z : CitizenI> updateMembers(workgroup: WorkgroupI, members: List<Member<Z>>): List<Member<CitizenBasic>> = requester
        .getFunction("update_workgroup_members")
        .select(
            "id" to workgroup.id,
            "members" to members
        )

    fun <W : WorkgroupWithMembersI<Z>, Z : CitizenI> updateMembers(workgroup: W): W {
        updateMembers(workgroup, workgroup.members).let {
            workgroup.members = it as List<Member<Z>>
        }

        return workgroup
    }

    class Filter(
        val createdById: String? = null,
        val members: List<UUID>? = null
    ) : Parameter
}
