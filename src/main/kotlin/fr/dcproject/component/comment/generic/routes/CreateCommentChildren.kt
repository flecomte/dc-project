package fr.dcproject.component.comment.generic.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentForUpdate
import fr.dcproject.component.comment.generic.database.CommentRef
import fr.dcproject.component.comment.generic.database.CommentRepository
import fr.dcproject.component.comment.toOutput
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object CreateCommentChildren {
    @Location("/comments/{comment}/children")
    class CreateCommentChildrenRequest(comment: UUID) {
        val comment = CommentRef(comment)
        class Input(val content: String)
    }

    fun Route.createCommentChildren(repo: CommentRepository, ac: CommentAccessControl) {
        post<CreateCommentChildrenRequest> {
            mustBeAuth()
            val parent = repo.findById(it.comment.id) ?: throw NotFoundException("Comment not found")
            val newComment = CommentForUpdate(
                content = call.receiveOrBadRequest<CreateCommentChildrenRequest.Input>().content,
                createdBy = citizen,
                parent = parent
            )

            ac.assert { canCreate(newComment, citizenOrNull) }
            repo.comment(newComment)

            call.respond(HttpStatusCode.Created, newComment.toOutput())
        }
    }
}
