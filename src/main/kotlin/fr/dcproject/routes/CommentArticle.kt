package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.ArticleRef
import fr.dcproject.entity.Citizen
import fr.dcproject.security.voter.CommentVoter.Action.CREATE
import fr.dcproject.security.voter.CommentVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.entity.request.Comment as CommentEntityRequest
import fr.dcproject.repository.CommentArticle as CommentArticleRepository

@KtorExperimentalLocationsAPI
object CommentArticlePaths {
    @Location("/articles/{article}/comments")
    class ArticleCommentRequest(
        val article: ArticleRef,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }

    @Location("/citizens/{citizen}/comments/articles")
    class CitizenCommentArticleRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.commentArticle(repo: CommentArticleRepository) {
    get<CommentArticlePaths.ArticleCommentRequest> {
        val comment = repo.findByTarget(it.article, it.page, it.limit)
        assertCan(VIEW, comment.result)
        call.respond(HttpStatusCode.OK, comment)
    }

    post<CommentArticlePaths.ArticleCommentRequest> {
        val content = call.receive<CommentEntityRequest>().content
        val comment = CommentEntity(
            target = it.article,
            createdBy = citizen,
            content = content
        )

        assertCan(CREATE, comment)
        repo.comment(comment)

        call.respond(HttpStatusCode.Created, comment)
    }

    get<CommentArticlePaths.CitizenCommentArticleRequest> {
        val comments = repo.findByCitizen(it.citizen)
        assertCan(VIEW, comments.result)
        call.respond(comments)
    }
}