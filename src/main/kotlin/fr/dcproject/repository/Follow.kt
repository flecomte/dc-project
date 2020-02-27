package fr.dcproject.repository

import fr.dcproject.entity.*
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.immutable.UuidEntity
import fr.postgresjson.repository.RepositoryI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.entity.Follow as FollowEntity

open class Follow<IN : TargetRef, OUT : TargetRef>(override var requester: Requester) : RepositoryI {
    open fun findByCitizen(
        citizen: CitizenI,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<FollowEntity<OUT>> =
        findByCitizen(citizen.id, page, limit)

    open fun findByCitizen(
        citizenId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<FollowEntity<OUT>> {
        return requester
            .getFunction("find_follows_by_citizen")
                .select(
                    page, limit,
                    "created_by_id" to citizenId
                )
    }

    fun follow(follow: FollowEntity<IN>) {
        requester
            .getFunction("follow")
            .sendQuery(
                "reference" to follow.target.reference,
                "target_id" to follow.target.id,
                "created_by_id" to follow.createdBy.id
            )
    }

    fun unfollow(follow: FollowEntity<IN>) {
        requester
            .getFunction("unfollow")
            .sendQuery(
                "reference" to follow.target.reference,
                "target_id" to follow.target.id,
                "created_by_id" to follow.createdBy.id
            )
    }

    open fun findFollow(
        citizen: CitizenI,
        target: UuidEntity
    ): FollowEntity<OUT>? =
        requester
            .getFunction("find_follow")
            .selectOne(
                "citizen_id" to citizen.id,
                "target_id" to target.id
            )
}

class FollowArticle(requester: Requester) : Follow<ArticleRef, ArticleEntity>(requester) {
    override fun findByCitizen(
        citizenId: UUID,
        page: Int,
        limit: Int
    ): Paginated<FollowEntity<ArticleEntity>> {
        return requester.run {
            getFunction("find_follows_article_by_citizen")
                .select(
                    page, limit,
                    "created_by_id" to citizenId
                )
        }
    }

    fun findFollowsByTarget(
        target: UuidEntity,
        page: Int = 1,
        limit: Int = 300
    ): Paginated<FollowSimple<ArticleRef, CitizenRef>> {
        return requester
            .getFunction("find_follows_article_by_target")
            .select(page, limit,
                "target_id" to target.id
            )
    }

    fun findFollowsByTarget(
        target: UuidEntity,
        limit: Int = 300
    ): Flow<FollowSimple<ArticleRef, CitizenRef>> = flow {
        var nextPage = 1
        do {
            val paginate = findFollowsByTarget(target, nextPage, limit)
            paginate.result.forEach {
                emit(it)
            }
            nextPage = paginate.currentPage+1
        } while (!paginate.isLastPage())
    }
}

class FollowConstitution(requester: Requester) : Follow<ConstitutionRef, ConstitutionEntity>(requester) {
    override fun findByCitizen(
        citizenId: UUID,
        page: Int,
        limit: Int
    ): Paginated<FollowEntity<ConstitutionEntity>> {
        return requester.run {
            getFunction("find_follows_constitution_by_citizen")
                .select(
                    page, limit,
                    "created_by_id" to citizenId
                )
        }
    }
}
