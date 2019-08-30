package fr.dcproject.repository

import fr.dcproject.entity.Article
import fr.dcproject.entity.Constitution
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.UuidEntity
import fr.postgresjson.repository.RepositoryI
import kotlin.reflect.KClass
import fr.dcproject.entity.Vote as VoteEntity

open class Vote <T: UuidEntity>(override var requester: Requester): RepositoryI<VoteEntity<T>> {
    override val entityName = VoteEntity::class as KClass<VoteEntity<T>>

    fun vote(vote: VoteEntity<T>) {
        val reference = vote.target::class.simpleName!!.toLowerCase()
        val author = vote.createdBy ?: error("vote must be contain an author")
        val anonymous = author.voteAnonymous
        requester
            .getFunction("vote")
            .sendQuery(
                "reference" to reference,
                "target_id" to vote.target.id,
                "note" to vote.note,
                "created_by_id" to author.id,
                "anonymous" to anonymous
            )
    }
}

class VoteArticle (requester: Requester): Vote<Article>(requester)
class VoteConstitution (requester: Requester): Vote<Constitution>(requester)