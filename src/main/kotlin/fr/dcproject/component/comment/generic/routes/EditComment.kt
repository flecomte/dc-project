package fr.dcproject.component.comment.generic.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentRef
import fr.dcproject.component.comment.generic.database.CommentRepository
import fr.dcproject.component.comment.toOutput
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object EditComment {
    @Location("/comments/{comment}")
    class EditCommentRequest(comment: UUID) {
        val comment = CommentRef(comment)
        class Input(val content: String)
    }

    fun Route.editComment(repo: CommentRepository, ac: CommentAccessControl) {
        put<EditCommentRequest> {
            val comment = repo.findById(it.comment.id) ?: throw NotFoundException("Comment not found")
            ac.assert { canUpdate(comment, citizenOrNull) }

            comment.content = call.receiveOrBadRequest<EditCommentRequest.Input>().content
            repo.edit(comment)

            call.respond(
                HttpStatusCode.OK,
                comment.toOutput()
            )
        }
    }
}
