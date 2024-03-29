package fr.dcproject.component.opinion.database

import com.fasterxml.jackson.core.type.TypeReference
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.citizen.database.CitizenI
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import net.pearx.kasechange.toSnakeCase
import java.util.UUID
import fr.dcproject.component.citizen.database.Citizen as CitizenEntity
import fr.dcproject.component.opinion.database.Opinion as OpinionEntity
import fr.dcproject.component.opinion.database.OpinionChoice as OpinionChoiceEntity

open class OpinionChoiceRepository(override val requester: Requester) : RepositoryI {
    /**
     * find all opinion choices
     * can be filtered by target compatibility
     */
    fun findOpinionsChoices(targets: List<String> = emptyList()): List<OpinionChoiceEntity> =
        requester
            .getFunction("find_opinion_choices")
            .select(
                "targets" to targets
            )

    /**
     * find opinion choices by name
     */
    fun findOpinionsChoiceByName(name: String): OpinionChoiceEntity? =
        findOpinionsChoices().first {
            it.name == name
        }

    /**
     * find one opinion choices by id
     */
    fun findOpinionChoiceById(id: UUID): OpinionChoiceEntity? =
        requester
            .getFunction("find_opinion_choice_by_id")
            .selectOne(
                "id" to id
            )

    /**
     * find one opinion choices by id
     */
    fun findOpinionChoicesByIds(ids: List<UUID>): List<OpinionChoiceEntity> =
        requester
            .getFunction("find_opinion_choices_by_ids")
            .select(
                "ids" to ids
            )

    fun upsertOpinionChoice(opinionChoice: OpinionChoiceEntity): OpinionChoiceEntity = requester
        .getFunction("upsert_opinion_choice")
        .selectOne(
            "resource" to opinionChoice
        )!!
}

abstract class OpinionRepository<T : TargetRef>(requester: Requester) : OpinionChoiceRepository(requester) {
    /**
     * Create an Opinion on target (article,...)
     */
    abstract fun updateOpinions(opinions: List<OpinionForUpdate<T>>): List<OpinionEntity<T>>
    fun updateOpinions(opinion: OpinionForUpdate<T>): List<OpinionEntity<T>> =
        updateOpinions(listOf(opinion))

    abstract fun addOpinion(opinion: OpinionForUpdate<T>): OpinionEntity<T>

    /**
     * Find opinions of one citizen filtered by target ids
     */
    fun findCitizenOpinionsByTargets(
        citizen: CitizenI,
        targets: List<UUID>
    ): List<OpinionEntity<T>> {
        val typeReference = object : TypeReference<List<OpinionEntity<T>>>() {}
        return requester.run {
            getFunction("find_citizen_opinions_by_target_ids")
                .select(
                    typeReference,
                    mapOf(
                        "citizen_id" to citizen.id,
                        "ids" to targets
                    )
                )
        }
    }

    /**
     * find opinion of citizen filtered by one target id
     */
    fun findCitizenOpinionsByTarget(
        citizen: CitizenEntity,
        target: UUID
    ): List<OpinionEntity<T>> {
        val typeReference = object : TypeReference<List<OpinionEntity<T>>>() {}
        return requester
            .getFunction("find_citizen_opinions_by_target_id")
            .select(
                typeReference,
                mapOf(
                    "citizen_id" to citizen.id,
                    "id" to target
                )
            )
    }

    /**
     * find paginated opinion of one citizen
     * can be sorted
     */
    fun findCitizenOpinions(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50,
        sort: String? = null,
        direction: RepositoryI.Direction? = null
    ): Paginated<OpinionEntity<TargetRef>> {
        return requester
            .getFunction("find_citizen_opinions")
            .select(
                page,
                limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "citizen_id" to citizen.id
            )
    }
}

class OpinionRepositoryArticle(requester: Requester) : OpinionRepository<ArticleRef>(requester) {
    /**
     * Update Opinions on Article (Delete old one)
     */
    override fun updateOpinions(opinions: List<OpinionForUpdate<ArticleRef>>): List<OpinionEntity<ArticleRef>> {
        return requester
            /* TODO change SQL function to not use .first() and pass all createdBy and target */
            .getFunction("update_citizen_opinions_by_target_id")
            .select(
                "choices_ids" to opinions.map { it.choice.id },
                "citizen_id" to opinions.first().createdBy.id,
                "target_id" to opinions.first().target.id,
                "target_reference" to opinions.first().target.reference
            )
    }

    /**
     * Add Opinions on Article
     */
    override fun addOpinion(opinion: OpinionForUpdate<ArticleRef>): OpinionEntity<ArticleRef> {
        return requester
            .getFunction("upsert_opinion")
            .selectOne("resource" to opinion)!!
    }
}
