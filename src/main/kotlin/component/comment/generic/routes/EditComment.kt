package fr.dcproject.component.comment.generic.routes

import fr.dcproject.component.auth.citizenOrNull
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
@Location("/comments/{comment}")
class EditCommentRequest(val comment: CommentRef)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.editComment(repo: CommentRepository, voter: CommentVoter) {
    put<EditCommentRequest> {
        val comment = repo.findById(it.comment.id) ?: throw NotFoundException("Comment not found")
        voter.assert { canUpdate(comment, citizenOrNull) }

        comment.content = call.receiveText()
        repo.edit(comment)

        call.respond(HttpStatusCode.OK, comment)
    }
}