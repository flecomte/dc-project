package fr.dcproject.component.comment.article.routes

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
@Location("/articles/{article}/comments")
class ArticleCommentsRequest(
    val article: ArticleRef,
    page: Int = 1,
    limit: Int = 50,
    val search: String? = null,
    sort: String = CommentArticleRepository.Sort.CREATED_AT.sql
) {
    val page: Int = if (page < 1) 1 else page
    val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    val sort: CommentArticleRepository.Sort = CommentArticleRepository.Sort.fromString(sort) ?: CommentArticleRepository.Sort.CREATED_AT
}

@KtorExperimentalLocationsAPI
fun Route.getArticleComments(repo: CommentArticleRepository, voter: CommentVoter) {
    get<ArticleCommentsRequest> {
        val comment = repo.findByTarget(it.article, it.page, it.limit, it.sort)
        if (comment.result.isNotEmpty()) {
            voter.assert { canView(comment.result, citizenOrNull) }
        }
        call.respond(HttpStatusCode.OK, comment)
    }
}
