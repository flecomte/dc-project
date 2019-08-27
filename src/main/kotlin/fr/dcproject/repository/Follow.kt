package fr.dcproject.repository

import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.UuidEntity
import fr.postgresjson.repository.RepositoryI
import java.util.*
import kotlin.reflect.KClass
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.entity.Follow as FollowEntity

open class Follow <T: UuidEntity>(override var requester: Requester): RepositoryI<FollowEntity<T>> {
    override val entityName = FollowEntity::class as KClass<FollowEntity<T>>
    open fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<FollowEntity<T>> =
        findByCitizen(citizen.id ?: error("The citizen must have an id"), page, limit)

    open fun findByCitizen(
        citizenId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<FollowEntity<T>> {
        return requester.run {
            getFunction("find_follows_by_citizen")
            .select(page, limit,
                "created_by_id" to citizenId
            )
        }
    }

    fun follow(follow: FollowEntity<T>) {
        val reference = follow.target::class.simpleName!!.toLowerCase()
        requester
            .getFunction("follow")
            .sendQuery(
                "reference" to reference,
                "target_id" to follow.target.id,
                "created_by_id" to follow.createdBy?.id
            )
    }

    fun unfollow(follow: FollowEntity<T>) {
        val reference = follow.target::class.simpleName!!.toLowerCase()
        requester
            .getFunction("unfollow")
            .sendQuery(
                "reference" to reference,
                "target_id" to follow.target.id,
                "created_by_id" to follow.createdBy?.id
            )
    }
}

class FollowArticle (requester: Requester): Follow<ArticleEntity>(requester) {
    override fun findByCitizen(
        citizenId: UUID,
        page: Int,
        limit: Int
    ): Paginated<FollowEntity<ArticleEntity>> {
        return requester.run {
            getFunction("find_follows_article_by_citizen")
                .select(page, limit,
                    "created_by_id" to citizenId
                )
        }
    }
}

class FollowConstitution (requester: Requester): Follow<ConstitutionEntity>(requester) {
    override fun findByCitizen(
        citizenId: UUID,
        page: Int,
        limit: Int
    ): Paginated<FollowEntity<ConstitutionEntity>> {
        return requester.run {
            getFunction("find_follows_constitution_by_citizen")
                .select(page, limit,
                    "created_by_id" to citizenId
                )
        }
    }
}
