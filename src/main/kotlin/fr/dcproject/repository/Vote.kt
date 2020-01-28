package fr.dcproject.repository

import com.fasterxml.jackson.core.type.TypeReference
import fr.dcproject.entity.*
import fr.dcproject.entity.Article
import fr.dcproject.entity.Comment
import fr.dcproject.entity.Constitution
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import java.util.*
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.entity.Vote as VoteEntity

open class Vote <T : TargetI>(override var requester: Requester) : RepositoryI {
    fun vote(vote: VoteEntity<T>): VoteAggregation {
        val author = vote.createdBy
        val anonymous = author.voteAnonymous
        return requester
            .getFunction("vote")
            .selectOne(
                "reference" to vote.target.reference,
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
        val typeReference = object : TypeReference<List<VoteEntity<TargetRef>>>() {}
        return requester.run {
            getFunction("find_citizen_votes_by_target_ids")
                .select(typeReference, mapOf(
                    "citizen_id" to citizen.id,
                    "ids" to targets
                ))
        }
    }
}

class VoteArticle(requester: Requester) : Vote<Article>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<Article>> =
        findByCitizen(
            citizen.id,
            "article",
            object : TypeReference<List<VoteEntity<Article>>>() {},
            page,
            limit
        )
}

class VoteArticleComment(requester: Requester) : Vote<Comment<Article>>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<Comment<Article>>> =
        findByCitizen(
            citizen.id,
            "article",
            object : TypeReference<List<VoteEntity<Comment<Article>>>>() {},
            page,
            limit
        )
}

class VoteComment(requester: Requester) : Vote<Comment<TargetRef>>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<Comment<TargetRef>>> =
        findByCitizen(
            citizen.id,
            "article",
            object : TypeReference<List<VoteEntity<Comment<TargetRef>>>>() {},
            page,
            limit
        )
}

class VoteConstitution(requester: Requester) : Vote<Constitution>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<Constitution>> =
        findByCitizen(
            citizen.id,
            "constitution",
            object : TypeReference<List<VoteEntity<Constitution>>>() {},
            page,
            limit
        )
}