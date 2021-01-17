package fr.dcproject.component.comment.generic.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentRef
import fr.dcproject.component.comment.generic.CommentRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

@KtorExperimentalLocationsAPI
@Location("/comments/{comment}/children")
class CreateCommentChildrenRequest(val comment: CommentRef) {
    class Input(val content: String)
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.createCommentChildren(repo: CommentRepository, voter: CommentVoter) {
    post<CreateCommentChildrenRequest> {
        val parent = repo.findById(it.comment.id) ?: throw NotFoundException("Comment not found")
        val newComment = CommentForUpdate(
            content = call.receive<CreateCommentChildrenRequest.Input>().content,
            createdBy = citizen,
            parent = parent
        )

        voter.assert { canCreate(newComment, citizenOrNull) }
        repo.comment(newComment)

        call.respond(HttpStatusCode.Created, newComment)
    }
}
