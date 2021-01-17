package fr.dcproject.component.comment.article.routes

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
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

@KtorExperimentalLocationsAPI
fun Route.createCommentArticle(repo: CommentArticleRepository, voter: CommentVoter) {
    post<PostArticleCommentRequest> {
        it.getComment(call).let { comment ->
            voter.assert { canCreate(comment, citizenOrNull) }
            repo.comment(comment)
            call.respond(HttpStatusCode.Created, comment)
        }
    }
}