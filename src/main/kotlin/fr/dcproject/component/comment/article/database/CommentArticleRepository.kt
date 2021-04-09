package fr.dcproject.component.comment.article.database

import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.TargetI
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenCreatorI
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.comment.generic.database.CommentForView
import fr.dcproject.component.comment.generic.database.CommentRepositoryAbs
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import java.util.UUID

class CommentArticleRepository(requester: Requester) : CommentRepositoryAbs<ArticleForView>(requester) {
    override fun findById(id: UUID): CommentForView<ArticleForView, CitizenCreatorI>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne<CommentForView<ArticleForView, CitizenCreator>>(mapOf("id" to id))
            as CommentForView<ArticleForView, CitizenCreatorI>?
    }

    override fun findByCitizen(
        citizen: CitizenI,
        page: Int,
        limit: Int
    ): Paginated<CommentForView<ArticleForView, CitizenCreatorI>> {
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select<CommentForView<ArticleForView, CitizenCreator>>(
                    page,
                    limit,
                    "created_by_id" to citizen.id,
                    "reference" to TargetI.getReference(ArticleRef::class)
                ) as Paginated<CommentForView<ArticleForView, CitizenCreatorI>>
        }
    }

    override fun findByTarget(
        target: EntityI,
        page: Int,
        limit: Int,
        sort: String
    ): Paginated<CommentForView<ArticleForView, CitizenCreatorI>> {
        return requester
            .getFunction("find_comments_by_target")
            .select<CommentForView<ArticleForView, CitizenCreator>>(
                page,
                limit,
                "target_id" to target.id,
                "sort" to sort
            ) as Paginated<CommentForView<ArticleForView, CitizenCreatorI>>
    }
}
