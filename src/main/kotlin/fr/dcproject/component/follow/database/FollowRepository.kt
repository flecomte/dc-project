package fr.dcproject.component.follow.database

import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.citizen.database.Citizen
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.constitution.database.ConstitutionForView
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

sealed class FollowRepository<IN : TargetRef, OUT : TargetRef>(override var requester: Requester) : RepositoryI {
    open fun findByCitizen(
        citizen: CitizenI,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<FollowForView<OUT>> =
        findByCitizen(citizen.id, page, limit)

    open fun findByCitizen(
        citizenId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<FollowForView<OUT>> {
        return requester
            .getFunction("find_follows_by_citizen")
            .select(
                page,
                limit,
                "created_by_id" to citizenId
            )
    }

    fun follow(follow: FollowForUpdate<IN, *>) {
        requester
            .getFunction("follow")
            .sendQuery(
                "reference" to follow.target.reference,
                "target_id" to follow.target.id,
                "created_by_id" to follow.createdBy.id
            )
    }

    fun unfollow(follow: FollowForUpdate<IN, *>) {
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
        target: TargetRef
    ): FollowForView<TargetRef>? =
        requester
            .getFunction("find_follow")
            .selectOne(
                "citizen_id" to citizen.id,
                "target_id" to target.id,
                "target_reference" to target.reference
            )

    fun findFollowsByTarget(
        target: Entity,
        bulkSize: Int = 300
    ): Flow<FollowForView<IN>> = flow {
        var lastId: UUID? = null
        while (true) {
            val result = findFollowsByTarget(target, lastId, bulkSize)
            if (result.count() == 0) {
                break
            }
            result.forEach {
                emit(it)
            }
            lastId = result.last().id
        }
    }

    abstract fun findFollowsByTarget(
        target: Entity,
        lastId: UUID?,
        limit: Int = 300
    ): List<FollowForView<IN>>
}

class FollowArticleRepository(requester: Requester) : FollowRepository<ArticleRef, ArticleForView>(requester) {
    override fun findByCitizen(
        citizenId: UUID,
        page: Int,
        limit: Int
    ): Paginated<FollowForView<ArticleForView>> {
        return requester.run {
            getFunction("find_follows_article_by_citizen")
                .select(
                    page,
                    limit,
                    "created_by_id" to citizenId
                )
        }
    }

    override fun findFollowsByTarget(
        target: Entity,
        lastId: UUID?,
        limit: Int
    ): List<FollowForView<ArticleRef>> {
        return requester
            .getFunction("find_follows_article_by_target")
            .select(
                "start_id" to lastId,
                "limit" to limit,
                "target_id" to target.id
            )
    }
}

class FollowConstitutionRepository(requester: Requester) : FollowRepository<ConstitutionRef, ConstitutionForView>(requester) {
    override fun findByCitizen(
        citizenId: UUID,
        page: Int,
        limit: Int
    ): Paginated<FollowForView<ConstitutionForView>> {
        return requester.run {
            getFunction("find_follows_constitution_by_citizen")
                .select(
                    page,
                    limit,
                    "created_by_id" to citizenId
                )
        }
    }

    override fun findFollowsByTarget(
        target: Entity,
        lastId: UUID?,
        limit: Int
    ): List<FollowForView<ConstitutionRef>> {
        TODO("Not yet implemented")
    }
}

class FollowCitizenRepository(requester: Requester) : FollowRepository<CitizenRef, Citizen>(requester) {
    override fun findByCitizen(
        citizenId: UUID,
        page: Int,
        limit: Int
    ): Paginated<FollowForView<Citizen>> {
        return requester.run {
            getFunction("find_follows_citizen_by_citizen")
                .select(
                    page,
                    limit,
                    "created_by_id" to citizenId
                )
        }
    }

    override fun findFollowsByTarget(
        target: Entity,
        lastId: UUID?,
        limit: Int
    ): List<FollowForView<CitizenRef>> {
        TODO("Not yet implemented")
    }
}
