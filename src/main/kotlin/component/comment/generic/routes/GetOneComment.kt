package fr.dcproject.component.comment.generic.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.CommentRef
import fr.dcproject.component.comment.generic.CommentRepository
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object GetOneComment {
    @Location("/comments/{comment}")
    class CommentRequest(val comment: CommentRef)

    fun Route.getOneComment(repo: CommentRepository, ac: CommentAccessControl) {
        get<CommentRequest> {
            val comment = repo.findById(it.comment.id) ?: throw NotFoundException("Comment ${it.comment.id} not found")
            ac.assert { canView(comment, citizenOrNull) }

            call.respond(HttpStatusCode.OK, comment)
        }
    }
}
