package fr.dcproject.repository

import fr.dcproject.entity.ArticleRef
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.entity.TargetI
import fr.dcproject.entity.TargetRef
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.immutable.UuidEntityI
import fr.postgresjson.repository.RepositoryI
import java.util.*
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.entity.Article as ArticleEntity

abstract class Comment<T : TargetI>(override var requester: Requester) : RepositoryI {
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
        return findByParent(parent.id, page, limit)
    }

    open fun findByParent(
        parentId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentEntity<T>> {
        return requester.run {
            getFunction("find_comments_by_parent")
                .select(
                    page, limit,
                    "parent_id" to parentId
                )
        }
    }

    open fun findByTarget(
        target: UuidEntityI,
        page: Int = 1,
        limit: Int = 50,
        sort: CommentArticle.Sort = CommentArticle.Sort.CREATED_AT
    ): Paginated<CommentEntity<T>> {
        return findByTarget(target.id, page, limit, sort)
    }

    open fun findByTarget(
        targetId: UUID,
        page: Int = 1,
        limit: Int = 50,
        sort: CommentArticle.Sort = CommentArticle.Sort.CREATED_AT
    ): Paginated<CommentEntity<T>> {
        return requester.run {
            getFunction("find_comments_by_target")
                .select(
                    page, limit,
                    "target_id" to targetId,
                    "sort" to sort.sql
                )
        }
    }

    fun <I : T> comment(comment: CommentEntity<I>) {
        requester
            .getFunction("comment")
            .sendQuery(
                "reference" to comment.target.reference,
                "resource" to comment
            )
    }

    fun <I : T> edit(comment: CommentEntity<I>) {
        requester
            .getFunction("edit_comment")
            .sendQuery(
                "id" to comment.id,
                "content" to comment.content
            )
    }
}

class CommentGeneric(requester: Requester) : Comment<TargetRef>(requester) {
    override fun findById(id: UUID): CommentEntity<TargetRef>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenEntity,
        page: Int,
        limit: Int
    ): Paginated<CommentEntity<TargetRef>> {
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select(
                    page, limit,
                    "created_by_id" to citizen.id
                )
        }
    }

    override fun findByParent(
        parentId: UUID,
        page: Int,
        limit: Int
    ): Paginated<CommentEntity<TargetRef>> {
        return requester.run {
            getFunction("find_comments_by_parent")
                .select(
                    page, limit,
                    "parent_id" to parentId
                )
        }
    }
}

class CommentArticle(requester: Requester) : Comment<ArticleEntity>(requester) {
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
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select(
                    page, limit,
                    "created_by_id" to citizen.id,
                    "reference" to TargetI.getReference(ArticleRef::class)
                )
        }
    }

    override fun findByTarget(
        target: UuidEntityI,
        page: Int,
        limit: Int,
        sort: Sort
    ): Paginated<CommentEntity<ArticleEntity>> = requester
        .getFunction("find_comments_by_target")
        .select(
            page, limit,
            "target_id" to target.id,
            "sort" to sort.sql
        )

    enum class Sort(val sql: String) {
        CREATED_AT("created_at"), VOTES("votes");

        companion object {
            fun fromString(string: String): Sort? {
                return values().firstOrNull { it.sql == string }
            }
        }
    }
}

class CommentConstitution(requester: Requester) : Comment<ConstitutionRef>(requester) {
    override fun findById(id: UUID): CommentEntity<ConstitutionRef>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenEntity,
        page: Int,
        limit: Int
    ): Paginated<CommentEntity<ConstitutionRef>> {
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select(
                    page, limit,
                    "created_by_id" to citizen.id,
                    "reference" to TargetI.getReference(ConstitutionRef::class)
                )
        }
    }

    override fun findByTarget(
        target: UuidEntityI,
        page: Int,
        limit: Int,
        sort: CommentArticle.Sort
    ): Paginated<CommentEntity<ConstitutionRef>> {
        return requester.run {
            getFunction("find_comments_by_target")
                .select(
                    page, limit,
                    "target_id" to target.id,
                    "sort" to sort.sql
                )
        }
    }
}