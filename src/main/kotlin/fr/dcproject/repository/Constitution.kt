package fr.dcproject.repository

import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import fr.postgresjson.repository.RepositoryI.Direction
import net.pearx.kasechange.toSnakeCase
import java.util.*
import fr.dcproject.entity.Constitution as ConstitutionEntity

class Constitution(override var requester: Requester) : RepositoryI<ConstitutionEntity> {
    override val entityName = ConstitutionEntity::class

    fun findById(id: UUID): ConstitutionEntity? {
        val function = requester.getFunction("find_constitution_by_id")
        return function.selectOne("id" to id)
    }

    fun find(
        page: Int = 1,
        limit: Int = 50,
        sort: String? = null,
        direction: Direction? = null,
        search: String? = null
    ): Paginated<ConstitutionEntity> {
        return requester
            .getFunction("find_constitutions")
            .select(
                page, limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "search" to search
            )
    }

    fun upsert(constitution: ConstitutionEntity): ConstitutionEntity? {
        return requester
            .getFunction("upsert_constitution")
            .selectOne("resource" to constitution)
    }
}
