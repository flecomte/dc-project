package fr.dcproject.repository

import com.fasterxml.jackson.core.type.TypeReference
import fr.dcproject.entity.Article
import fr.dcproject.entity.Comment
import fr.dcproject.entity.Constitution
import fr.dcproject.entity.VoteAggregation
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.UuidEntity
import fr.postgresjson.repository.RepositoryI
import java.util.*
import kotlin.reflect.KClass
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.entity.Vote as VoteEntity

open class Vote <T: UuidEntity>(override var requester: Requester): RepositoryI<VoteEntity<T>> {
    override val entityName = VoteEntity::class as KClass<VoteEntity<T>>

    fun vote(vote: VoteEntity<T>): VoteAggregation {
        val target = vote.target
        val reference = if (target is Comment<*>) {
            target::class.simpleName!!.toLowerCase() +
            "_on_" +
            target.targetReference
        } else {
            target::class.simpleName!!.toLowerCase()
        }
        val author = vote.createdBy ?: error("vote must be contain an author")
        val anonymous = author.voteAnonymous
        return requester
            .getFunction("vote")
            .selectOne(
                "reference" to reference,
                "target_id" to vote.target.id,
                "note" to vote.note,
                "created_by_id" to author.id,
                "anonymous" to anonymous
            )!!
    }

    fun findByCitizen(
        citizenId: UUID,
        target: String,
        typeReference: TypeReference<List<VoteEntity<T>>>,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<T>> {
        return requester.run {
            getFunction("find_votes_by_citizen")
            .select(page, limit, typeReference, mapOf(
                "created_by_id" to citizenId,
                "reference" to target
            ))
        }
    }

    fun findCitizenVotesByTargets(
        citizen: CitizenEntity,
        targets: List<UUID>
    ): List<VoteEntity<*>> {
        val typeReference = object: TypeReference<List<VoteEntity<UuidEntity>>>() {}
        return requester.run {
            val citizenId = citizen.id ?: error("The citizen must have an id")
            getFunction("find_citizen_votes_by_target_ids")
                .select(typeReference, mapOf(
                    "citizen_id" to citizenId,
                    "ids" to targets
                ))
        }
    }
}

class VoteArticle (requester: Requester): Vote<Article>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<Article>> =
        findByCitizen(
            citizen.id ?: error("The citizen must have an id"),
            "article",
            object: TypeReference<List<VoteEntity<Article>>>() {},
            page,
            limit
        )
}

class VoteArticleComment (requester: Requester): Vote<Comment<Article>>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<Comment<Article>>> =
        findByCitizen(
            citizen.id ?: error("The citizen must have an id"),
            "article",
            object: TypeReference<List<VoteEntity<Comment<Article>>>>() {},
            page,
            limit
        )
}

class VoteComment (requester: Requester): Vote<Comment<UuidEntity>>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<Comment<UuidEntity>>> =
        findByCitizen(
            citizen.id ?: error("The citizen must have an id"),
            "article",
            object: TypeReference<List<VoteEntity<Comment<UuidEntity>>>>() {},
            page,
            limit
        )
}

class VoteConstitution (requester: Requester): Vote<Constitution>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<Constitution>> =
        findByCitizen(
            citizen.id ?: error("The citizen must have an id"),
            "constitution",
            object: TypeReference<List<VoteEntity<Constitution>>>() {},
            page,
            limit
        )
}