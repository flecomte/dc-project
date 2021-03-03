package fr.dcproject.component.comment.article.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.article.database.CommentArticleRepository
import fr.dcproject.component.comment.article.routes.CreateCommentArticle.PostArticleCommentRequest.Input
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentForUpdate
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object CreateCommentArticle {
    @Location("/articles/{article}/comments")
    class PostArticleCommentRequest(article: UUID) {
        val article = ArticleRef(article)
        class Input(val content: String)
    }

    suspend fun PostArticleCommentRequest.getComment(call: ApplicationCall) = call.receiveOrBadRequest<Input>().run {
        CommentForUpdate(
            target = article,
            createdBy = call.citizen,
            content = content
        )
    }

    fun Route.createCommentArticle(repo: CommentArticleRepository, ac: CommentAccessControl) {
        post<PostArticleCommentRequest> {
            it.getComment(call).let { comment ->
                ac.assert { canCreate(comment, citizenOrNull) }
                repo.comment(comment)
                call.respond(HttpStatusCode.Created, comment)
            }
        }
    }
}
