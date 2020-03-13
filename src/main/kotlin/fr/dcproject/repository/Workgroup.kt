package fr.dcproject.repository

import fr.dcproject.entity.CitizenRef
import fr.dcproject.entity.WorkgroupSimple
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.Parameter
import fr.postgresjson.repository.RepositoryI
import fr.postgresjson.repository.RepositoryI.Direction
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

    class Filter(
        val createdById: String? = null
    ) : Parameter
}
