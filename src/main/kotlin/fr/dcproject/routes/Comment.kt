package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.Comment
import fr.dcproject.routes.CommentPaths.CreateCommentRequest.Content
import fr.dcproject.security.voter.CommentVoter.Action.*
import fr.dcproject.security.voter.assertCan
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI
import java.util.*
import fr.dcproject.repository.CommentGeneric as CommentRepository

@KtorExperimentalLocationsAPI
object CommentPaths {
    // TODO: change UUID by entity converter
    @Location("/comments/{comment}")
    class CommentRequest(val comment: UUID)

    @Location("/comments/{comment}/children")
    class CommentChildrenRequest(
        val comment: UUID,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }

    @Location("/comments/{comment}/children")
    class CreateCommentRequest(val comment: UUID) {
        class Content(val content: String)
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.comment(repo: CommentRepository) {
    get<CommentPaths.CommentRequest> {
        val comment = repo.findById(it.comment)!!
        assertCan(VIEW, comment)

        call.respond(HttpStatusCode.OK, comment)
    }

    get<CommentPaths.CommentChildrenRequest> {
        val comments =
            repo.findByParent(
                it.comment,
                it.page,
                it.limit
            )

        assertCan(VIEW, comments.result)

        call.respond(HttpStatusCode.OK, comments)
    }

    post<CommentPaths.CreateCommentRequest> {
        val parent = repo.findById(it.comment) ?: throw NotFoundException("Comment not found")
        val newComment = Comment(
            content = call.receive<Content>().content,
            createdBy = citizen,
            parent = parent
        )

        assertCan(CREATE, newComment)
        repo.comment(newComment)

        call.respond(HttpStatusCode.Created, newComment)
    }

    put<CommentPaths.CommentRequest> {
        val comment = repo.findById(it.comment)!!
        assertCan(UPDATE, comment)

        comment.content = call.receiveText()
        repo.edit(comment)

        call.respond(HttpStatusCode.OK, comment)
    }
}
