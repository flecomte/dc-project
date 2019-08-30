package fr.dcproject.routes

import fr.dcproject.citizen
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
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.repository.CommentArticle as CommentArticleRepository

@KtorExperimentalLocationsAPI
object CommentArticlePaths {
    @Location("/articles/{article}/comments") class ArticleCommentRequest(val article: ArticleEntity)
    @Location("/citizens/{citizen}/comments/articles") class CitizenCommentArticleRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.commentArticle(repo: CommentArticleRepository) {
    get<CommentArticlePaths.ArticleCommentRequest> {
        val comment = repo.findByTarget(it.article)
        assertCan(VIEW, comment.result)
        call.respond(HttpStatusCode.OK, comment)
    }

    post<CommentArticlePaths.ArticleCommentRequest> {
        assertCan(CREATE, it.article)

        val content = call.receiveText()
        val comment = CommentEntity(
            target = it.article,
            createdBy = citizen,
            content = content
        )
        repo.comment(comment)

        call.respond(HttpStatusCode.Created, comment)
    }

    get<CommentArticlePaths.CitizenCommentArticleRequest> {
        val comments = repo.findByCitizen(it.citizen)
        assertCan(VIEW, comments.result)
        call.respond(comments)
    }
}