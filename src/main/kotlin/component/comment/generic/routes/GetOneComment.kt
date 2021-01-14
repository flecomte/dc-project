package fr.dcproject.component.comment.generic.routes

import fr.dcproject.component.comment.generic.CommentRef
import fr.dcproject.component.comment.generic.CommentRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.ktorVoter.assertCan
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

@KtorExperimentalLocationsAPI
@Location("/comments/{comment}")
class CommentRequest(val comment: CommentRef)


@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.getOneComment(repo: CommentRepository) {
    get<CommentRequest> {
        val comment = repo.findById(it.comment.id) ?: NotFoundException("Comment ${it.comment.id} not found")
        assertCan(CommentVoter.Action.VIEW, comment)

        call.respond(HttpStatusCode.OK, comment)
    }
}