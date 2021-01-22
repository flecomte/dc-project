package fr.dcproject.component.comment.generic.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentRef
import fr.dcproject.component.comment.generic.CommentRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object CreateCommentChildren {
    @Location("/comments/{comment}/children")
    class CreateCommentChildrenRequest(val comment: CommentRef) {
        class Input(val content: String)
    }

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
}