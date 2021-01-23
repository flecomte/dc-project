package fr.dcproject.component.constitution

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenWithUserI
import fr.dcproject.component.constitution.ConstitutionSimple.TitleSimple
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import fr.postgresjson.repository.RepositoryI.Direction
import net.pearx.kasechange.toSnakeCase
import java.util.UUID
import fr.dcproject.component.constitution.Constitution as ConstitutionEntity

class ConstitutionRepository(override var requester: Requester) : RepositoryI {
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
                page,
                limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "search" to search
            )
    }

    fun upsert(constitution: ConstitutionSimple<CitizenWithUserI, TitleSimple<ArticleRef>>): ConstitutionEntity? {
        return requester
            .getFunction("upsert_constitution")
            .selectOne("resource" to constitution)
    }
}
