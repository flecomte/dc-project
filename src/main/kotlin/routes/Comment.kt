package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.CommentForUpdate
import fr.dcproject.entity.CommentRef
import fr.dcproject.routes.CommentPaths.CreateCommentRequest.Content
import fr.dcproject.security.voter.CommentVoter.Action.*
import fr.ktorVoter.assertCan
import fr.ktorVoter.assertCanAll
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import java.util.*
import fr.dcproject.repository.CommentGeneric as CommentRepository

@KtorExperimentalLocationsAPI
object CommentPaths {
    @Location("/comments/{comment}")
    class CommentRequest(val comment: CommentRef)

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
    class CreateCommentRequest(val comment: CommentRef) {
        class Content(val content: String)
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.comment(repo: CommentRepository) {
    get<CommentPaths.CommentRequest> {
        val comment = repo.findById(it.comment.id)!!
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

        assertCanAll(VIEW, comments.result)

        call.respond(HttpStatusCode.OK, comments)
    }

    post<CommentPaths.CreateCommentRequest> {
        val parent = repo.findById(it.comment.id) ?: throw NotFoundException("Comment not found")
        val newComment = CommentForUpdate(
            content = call.receive<Content>().content,
            createdBy = citizen,
            parent = parent
        )

        assertCan(CREATE, newComment)
        repo.comment(newComment)

        call.respond(HttpStatusCode.Created, newComment)
    }

    put<CommentPaths.CommentRequest> {
        val comment = repo.findById(it.comment.id)!!
        assertCan(UPDATE, comment)


        comment.content = call.receiveText()
        repo.edit(comment)

        call.respond(HttpStatusCode.OK, comment)
    }
}
