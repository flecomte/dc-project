package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.citizenOrNull
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.article.CommentArticleRepository.Sort
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
object CommentArticlePaths {
    @Location("/articles/{article}/comments")
    class ArticleCommentRequest(
        val article: ArticleRef,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null,
        sort: String = Sort.CREATED_AT.sql
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
        val sort: Sort = Sort.fromString(sort) ?: Sort.CREATED_AT
    }

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

    @Location("/citizens/{citizen}/comments/articles")
    class CitizenCommentArticleRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.commentArticle(repo: CommentArticleRepository, voter: CommentVoter) {
    get<CommentArticlePaths.ArticleCommentRequest> {
        val comment = repo.findByTarget(it.article, it.page, it.limit, it.sort)
        if (comment.result.isNotEmpty()) {
            voter.assert { canView(comment.result, citizenOrNull) }
        }
        call.respond(HttpStatusCode.OK, comment)
    }

    post<CommentArticlePaths.PostArticleCommentRequest> {
        it.getComment(call).let { comment ->
            voter.assert { canCreate(comment, citizenOrNull) }
            repo.comment(comment)
            call.respond(HttpStatusCode.Created, comment)
        }
    }

    get<CommentArticlePaths.CitizenCommentArticleRequest> {
        repo.findByCitizen(it.citizen).let { comments ->
            voter.assert { canView(comments.result, citizenOrNull) }
            call.respond(comments)
        }
    }
}