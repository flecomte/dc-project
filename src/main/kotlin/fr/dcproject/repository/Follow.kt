package fr.dcproject.repository

import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.EntityI
import fr.postgresjson.repository.RepositoryI
import java.util.*
import kotlin.reflect.KClass
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Follow as FollowEntity

open class Follow <T: EntityI<UUID>>(override var requester: Requester): RepositoryI<FollowEntity<T>> {
    override val entityName = FollowEntity::class as KClass<FollowEntity<T>>

    fun follow(follow: FollowEntity<T>) {
        val reference = follow.target::class.simpleName!!.toLowerCase()
        requester
            .getFunction("follow")
            .sendQuery(
                "reference" to reference,
                "target_id" to follow.target.id,
                "citizen_id" to follow.citizen.id
            )
    }

    fun unfollow(follow: FollowEntity<T>) {
        val reference = follow.target::class.simpleName!!.toLowerCase()
        requester
            .getFunction("unfollow")
            .sendQuery(
                "reference" to reference,
                "target_id" to follow.target.id,
                "citizen_id" to follow.citizen.id
            )
    }
}
class FollowArticleRepository(override var requester: Requester): Follow<ArticleEntity>(requester)
