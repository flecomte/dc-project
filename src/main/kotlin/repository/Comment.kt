package fr.dcproject.repository

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.entity.*
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.UuidEntityI
import fr.postgresjson.repository.RepositoryI
import java.util.*

abstract class Comment<T : TargetI>(override var requester: Requester) : RepositoryI {
    abstract fun findById(id: UUID): CommentForView<T, CitizenRef>?

    abstract fun findByCitizen(
        citizen: CitizenI,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentForView<T, CitizenRef>>

    open fun findByParent(
        parent: CommentForView<T, CitizenRef>,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentForView<T, CitizenRef>> {
        return findByParent(parent.id, page, limit)
    }

    open fun findByParent(
        parentId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentForView<T, CitizenRef>> {
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
    ): Paginated<CommentForView<T, CitizenRef>> {
        return findByTarget(target.id, page, limit, sort)
    }

    open fun findByTarget(
        targetId: UUID,
        page: Int = 1,
        limit: Int = 50,
        sort: CommentArticle.Sort = CommentArticle.Sort.CREATED_AT
    ): Paginated<CommentForView<T, CitizenRef>> {
        return requester.run {
            getFunction("find_comments_by_target")
                .select(
                    page, limit,
                    "target_id" to targetId,
                    "sort" to sort.sql
                )
        }
    }

    fun <I : T, C: CitizenRef> comment(comment: CommentForUpdate<I, C>) {
        requester
            .getFunction("comment")
            .sendQuery(
                "reference" to comment.target.reference,
                "resource" to comment
            )
    }

    fun <I : T> edit(comment: CommentForUpdate<I, CitizenRef>) {
        requester
            .getFunction("edit_comment")
            .sendQuery(
                "id" to comment.id,
                "content" to comment.content
            )
    }
}

class CommentGeneric(requester: Requester) : Comment<TargetRef>(requester) {
    override fun findById(id: UUID): CommentForView<TargetRef, CitizenRef>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenI,
        page: Int,
        limit: Int
    ): Paginated<CommentForView<TargetRef, CitizenRef>> {
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
    ): Paginated<CommentForView<TargetRef, CitizenRef>> {
        return requester.run {
            getFunction("find_comments_by_parent")
                .select(
                    page, limit,
                    "parent_id" to parentId
                )
        }
    }
}

class CommentArticle(requester: Requester) : Comment<ArticleForView>(requester) {
    override fun findById(id: UUID): CommentForView<ArticleForView, CitizenRef>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenI,
        page: Int,
        limit: Int
    ): Paginated<CommentForView<ArticleForView, CitizenRef>> {
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
    ): Paginated<CommentForView<ArticleForView, CitizenRef>> = requester
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
    override fun findById(id: UUID): CommentForView<ConstitutionRef, CitizenRef>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenI,
        page: Int,
        limit: Int
    ): Paginated<CommentForView<ConstitutionRef, CitizenRef>> {
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
    ): Paginated<CommentForView<ConstitutionRef, CitizenRef>> {
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
