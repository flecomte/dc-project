package fr.dcproject.repository

import com.fasterxml.jackson.core.type.TypeReference
import fr.dcproject.entity.Article
import fr.dcproject.entity.OpinionAggregation
import fr.dcproject.entity.TargetRef
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import net.pearx.kasechange.toSnakeCase
import java.util.*
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.entity.Opinion as OpinionEntity
import fr.dcproject.entity.OpinionChoice as OpinionChoiceEntity

open class OpinionChoice(override val requester: Requester) : RepositoryI {
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
     * find one opinion choices by id
     */
    fun findOpinionChoiceById(id: UUID): OpinionChoiceEntity? =
        requester
            .getFunction("find_opinion_choice_by_id")
            .selectOne(
                "id" to id
            )
}

open class Opinion<T : TargetRef>(requester: Requester) : OpinionChoice(requester) {
    /**
     * Create an Opinion on target (article,...)
     */
    fun opinion(opinion: OpinionEntity<T>): OpinionAggregation {
        return requester
            .getFunction("opinion")
            .selectOne(
                "reference" to opinion.target.reference,
                "target_id" to opinion.target.id,
                "opinion" to opinion.id,
                "created_by_id" to opinion.createdBy
            )!!
    }

    /**
     * Find opinions of one citizen filtered by target ids
     */
    fun findCitizenOpinionsByTargets(
        citizen: CitizenEntity,
        targets: List<UUID>
    ): List<OpinionEntity<T>> {
        val typeReference = object : TypeReference<List<OpinionEntity<T>>>() {}
        return requester.run {
            getFunction("find_citizen_opinions_by_target_ids")
                .select(
                    typeReference, mapOf(
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
                typeReference, mapOf(
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
            .select(page, limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "citizen" to citizen.id
            )
    }
}

class OpinionArticle(requester: Requester) : Opinion<Article>(requester)