package fr.dcproject.component.vote

import com.fasterxml.jackson.core.type.TypeReference
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.comment.generic.CommentForView
import fr.dcproject.component.constitution.Constitution
import fr.dcproject.component.vote.entity.VoteAggregation
import fr.dcproject.component.vote.entity.VoteForUpdateI
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import java.util.UUID
import fr.dcproject.component.citizen.Citizen as CitizenEntity
import fr.dcproject.component.vote.entity.Vote as VoteEntity

abstract class VoteRepositoryAbs<T : TargetI>(override var requester: Requester) : RepositoryI {
    fun vote(vote: VoteForUpdateI<T, *>, anonymous: Boolean? = null): VoteAggregation {
        val author = vote.createdBy
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
                .select(
                    page,
                    limit,
                    typeReference,
                    mapOf(
                        "created_by_id" to citizenId,
                        "reference" to target
                    )
                )
        }
    }

    fun findCitizenVotesByTargets(
        citizen: CitizenEntity,
        targets: List<UUID>
    ): List<VoteEntity<*>> {
        val typeReference = object : TypeReference<List<VoteEntity<TargetRef>>>() {}
        return requester.run {
            getFunction("find_citizen_votes_by_target_ids")
                .select(
                    typeReference,
                    mapOf(
                        "citizen_id" to citizen.id,
                        "ids" to targets
                    )
                )
        }
    }
}

class VoteRepository(requester: Requester) : VoteRepositoryAbs<TargetRef>(requester)

class VoteArticleRepository(requester: Requester) : VoteRepositoryAbs<ArticleForView>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<ArticleForView>> =
        findByCitizen(
            citizen.id,
            "article",
            object : TypeReference<List<VoteEntity<ArticleForView>>>() {},
            page,
            limit
        )
}

class VoteArticleCommentRepository(requester: Requester) : VoteRepositoryAbs<CommentForView<ArticleForView, CitizenRef>>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<CommentForView<ArticleForView, CitizenRef>>> =
        findByCitizen(
            citizen.id,
            "article",
            object : TypeReference<List<VoteEntity<CommentForView<ArticleForView, CitizenRef>>>>() {},
            page,
            limit
        )
}

class VoteCommentRepository(requester: Requester) : VoteRepositoryAbs<CommentForView<TargetRef, CitizenRef>>(requester) {
    fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<VoteEntity<CommentForView<TargetRef, CitizenRef>>> =
        findByCitizen(
            citizen.id,
            "article",
            object : TypeReference<List<VoteEntity<CommentForView<TargetRef, CitizenRef>>>>() {},
            page,
            limit
        )
}

class VoteConstitutionRepository(requester: Requester) : VoteRepositoryAbs<Constitution>(requester) {
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
