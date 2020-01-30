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
        limit: Int = 50
    ): Paginated<CommentEntity<T>> {
        return findByTarget(target.id, page, limit)
    }

    open fun findByTarget(
        targetId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentEntity<T>> {
        return requester.run {
            getFunction("find_comments_by_target")
                .select(
                    page, limit,
                    "target_id" to targetId
                )
        }
    }

    fun comment(comment: CommentEntity<T>) {
        requester
            .getFunction("comment")
            .sendQuery(
                "reference" to comment.target.reference,
                "resource" to comment
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
}

class CommentArticle(requester: Requester) : Comment<ArticleRef>(requester) {
    override fun findById(id: UUID): CommentEntity<ArticleRef>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenEntity,
        page: Int,
        limit: Int
    ): Paginated<CommentEntity<ArticleRef>> {
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
        limit: Int
    ): Paginated<CommentEntity<ArticleRef>> {
        return requester.run {
            getFunction("find_comments_by_target")
                .select(
                    page, limit,
                    "target_id" to target.id
                )
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
        limit: Int
    ): Paginated<CommentEntity<ConstitutionRef>> {
        return requester.run {
            getFunction("find_comments_by_target")
                .select(
                    page, limit,
                    "target_id" to target.id
                )
        }
    }
}
