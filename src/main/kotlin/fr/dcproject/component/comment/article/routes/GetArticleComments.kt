package fr.dcproject.component.comment.article.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetArticleComments {
    @Location("/articles/{article}/comments")
    class ArticleCommentsRequest(
        article: UUID,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null,
        sort: String = CommentArticleRepository.Sort.CREATED_AT.sql
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        val article = ArticleRef(article)
        val sort: CommentArticleRepository.Sort = CommentArticleRepository.Sort.fromString(sort) ?: CommentArticleRepository.Sort.CREATED_AT
    }

    fun Route.getArticleComments(repo: CommentArticleRepository, ac: CommentAccessControl) {
        get<ArticleCommentsRequest> {
            val comment = repo.findByTarget(it.article, it.page, it.limit, it.sort)
            if (comment.result.isNotEmpty()) {
                ac.assert { canView(comment.result, citizenOrNull) }
            }
            call.respond(HttpStatusCode.OK, comment)
        }
    }
}
