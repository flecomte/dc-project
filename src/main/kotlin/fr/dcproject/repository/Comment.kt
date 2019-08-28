package fr.dcproject.repository

import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.UuidEntity
import fr.postgresjson.repository.RepositoryI
import java.util.*
import kotlin.reflect.KClass
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.entity.Constitution as ConstitutionEntity

abstract class Comment <T: UuidEntity>(override var requester: Requester): RepositoryI<CommentEntity<T>> {
    override val entityName = CommentEntity::class as KClass<CommentEntity<T>>

    abstract fun findById(id: UUID): CommentEntity<T>?

    abstract fun findByCitizen(
        citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentEntity<T>>

    open fun findByParent(
        parent: CommentEntity<T>,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentEntity<T>> {
        return findByParent(parent.id ?: error("comment must have an ID"), page, limit)
    }

    open fun findByParent(
        parentId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentEntity<T>> {
        return requester.run {
            getFunction("find_comments_by_parent")
            .select(page, limit,
                "parent_id" to parentId
            )
        }
    }

    open fun findByTarget(
        target: UuidEntity,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentEntity<T>> {
        return findByTarget(target.id ?: error("comment must have an ID"), page, limit)
    }

    open fun findByTarget(
        targetId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentEntity<T>> {
        return requester.run {
            getFunction("find_comments_by_target")
            .select(page, limit,
                "target_id" to targetId
            )
        }
    }

    fun comment(comment: CommentEntity<T>) {
        val reference = comment.target::class.simpleName!!.toLowerCase()
        requester
            .getFunction("comment")
            .sendQuery(
                "reference" to reference,
                "target_id" to comment.target.id,
                "created_by_id" to comment.createdBy?.id,
                "content" to comment.content
            )
    }

    fun edit(comment: CommentEntity<T>) {
        requester
            .getFunction("edit_comment")
            .sendQuery(
                "id" to comment.id,
                "content" to comment.content
            )
    }
}

class GenericTargetEntity(id: UUID = UUID.randomUUID()): UuidEntity(id)

class CommentGeneric (requester: Requester): Comment<GenericTargetEntity>(requester) {
    override fun findById(id: UUID): CommentEntity<GenericTargetEntity>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenEntity,
        page: Int,
        limit: Int
    ): Paginated<CommentEntity<GenericTargetEntity>> {
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select(page, limit,
                    "created_by_id" to citizen.id
                )
        }
    }
}

class CommentArticle (requester: Requester): Comment<ArticleEntity>(requester) {
    override fun findById(id: UUID): CommentEntity<ArticleEntity>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenEntity,
        page: Int,
        limit: Int
    ): Paginated<CommentEntity<ArticleEntity>> {
        val reference = ArticleEntity::class.simpleName!!.toLowerCase()
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select(page, limit,
                    "created_by_id" to citizen.id,
                    "reference" to reference
                )
        }
    }
}

class CommentConstitution (requester: Requester): Comment<ConstitutionEntity>(requester) {
    override fun findById(id: UUID): CommentEntity<ConstitutionEntity>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenEntity,
        page: Int,
        limit: Int
    ): Paginated<CommentEntity<ConstitutionEntity>> {
        val reference = ConstitutionEntity::class.simpleName!!.toLowerCase()
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select(page, limit,
                    "created_by_id" to citizen.id,
                    "reference" to reference
                )
        }
    }
}