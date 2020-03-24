package fr.dcproject.repository

import fr.dcproject.entity.*
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.Parameter
import fr.postgresjson.repository.RepositoryI
import fr.postgresjson.repository.RepositoryI.Direction
import fr.postgresjson.serializer.serialize
import net.pearx.kasechange.toSnakeCase
import java.util.*
import fr.dcproject.entity.Workgroup as WorkgroupEntity

class Workgroup(override var requester: Requester) : RepositoryI {
    fun findById(id: UUID): WorkgroupEntity? {
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
    ): Paginated<WorkgroupEntity> {
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

    fun upsert(workgroup: WorkgroupSimple<CitizenRef>): WorkgroupEntity = requester
        .getFunction("upsert_workgroup")
        .selectOne("resource" to workgroup) ?: error("query 'upsert_workgroup' return null")

    fun delete(workgroup: WorkgroupRef) = requester
            .getFunction("delete_workgroup")
            .perform("id" to workgroup.id)

    fun addMember(workgroup: WorkgroupI, member: CitizenI): List<CitizenBasic> =
        addMembers(workgroup, listOf(member))

    fun addMembers(workgroup: WorkgroupI, members: List<CitizenI>): List<CitizenBasic> = requester
        .getFunction("add_workgroup_members")
        .select(
            "id" to workgroup.id,
            "resource" to members.serialize()
        )

    fun removeMember(workgroup: WorkgroupI, memberToDelete: CitizenI): List<CitizenBasic> =
        removeMembers(workgroup, listOf(memberToDelete))

    fun removeMembers(workgroup: WorkgroupI, membersToDelete: List<CitizenI>): List<CitizenBasic> = requester
        .getFunction("remove_workgroup_members")
        .select(
            "id" to workgroup.id,
            "resource" to membersToDelete
        )

    fun updateMembers(workgroup: WorkgroupI, members: List<CitizenI>): List<CitizenBasic> = requester
        .getFunction("update_workgroup_members")
        .select(
            "id" to workgroup.id,
            "resource" to members
        )

    fun <W : WorkgroupWithMembersI<CitizenI>> updateMembers(workgroup: W): W {
        updateMembers(workgroup, workgroup.members).let {
            workgroup.members = it
        }

        return workgroup
    }

    class Filter(
        val createdById: String? = null
    ) : Parameter
}
