package fr.dcproject.component.comment.article

import fr.dcproject.common.entity.TargetI
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.comment.generic.CommentForView
import fr.dcproject.component.comment.generic.CommentRepositoryAbs
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.UuidEntityI
import java.util.UUID

class CommentArticleRepository(requester: Requester) : CommentRepositoryAbs<ArticleForView>(requester) {
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
                    page,
                    limit,
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
            page,
            limit,
            "target_id" to target.id,
            "sort" to sort.sql
        )

    enum class Sort(val sql: String) {
        CREATED_AT("created_at"),
        VOTES("votes");

        companion object {
            fun fromString(string: String): Sort? {
                return values().firstOrNull { it.sql == string }
            }
        }
    }
}
