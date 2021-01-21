package fr.dcproject.component.comment.article.routes

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.voter.assert
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object CreateCommentArticle {
    @Location("/articles/{article}/comments")
    class PostArticleCommentRequest(
        val article: ArticleForView
    ) {
        class Comment(
            val content: String
        )

        suspend fun getComment(call: ApplicationCall) = call.receive<Comment>().run {
            CommentForUpdate(
                target = article,
                createdBy = call.citizen,
                content = content
            )
        }
    }

    fun Route.createCommentArticle(repo: CommentArticleRepository, voter: CommentVoter) {
        post<PostArticleCommentRequest> {
            it.getComment(call).let { comment ->
                voter.assert { canCreate(comment, citizenOrNull) }
                repo.comment(comment)
                call.respond(HttpStatusCode.Created, comment)
            }
        }
    }
}
