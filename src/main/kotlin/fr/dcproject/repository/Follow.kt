package fr.dcproject.repository

import fr.dcproject.entity.CitizenI
import fr.dcproject.entity.TargetI
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.entity.Follow as FollowEntity

open class Follow<T : TargetI>(override var requester: Requester) : RepositoryI {
    open fun findByCitizen(
        citizen: CitizenI,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<FollowEntity<T>> =
        findByCitizen(citizen.id, page, limit)

    open fun findByCitizen(
        citizenId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<FollowEntity<T>> {
        return requester.run {
            getFunction("find_follows_by_citizen")
                .select(
                    page, limit,
                    "created_by_id" to citizenId
                )
        }
    }

    fun follow(follow: FollowEntity<T>) {
        requester
            .getFunction("follow")
            .sendQuery(
                "reference" to follow.target.reference,
                "target_id" to follow.target.id,
                "created_by_id" to follow.createdBy.id
            )
    }

    fun unfollow(follow: FollowEntity<T>) {
        requester
            .getFunction("unfollow")
            .sendQuery(
                "reference" to follow.target.reference,
                "target_id" to follow.target.id,
                "created_by_id" to follow.createdBy.id
            )
    }
}

class FollowArticle(requester: Requester) : Follow<ArticleEntity>(requester) {
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
}

class FollowConstitution(requester: Requester) : Follow<ConstitutionEntity>(requester) {
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
