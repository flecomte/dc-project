package fr.dcproject.component.comment.generic.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
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
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetOneComment {
    @Location("/comments/{comment}")
    class CommentRequest(comment: UUID) {
        val comment = CommentRef(comment)
    }

    fun Route.getOneComment(repo: CommentRepository, ac: CommentAccessControl) {
        get<CommentRequest> {
            val comment = repo.findById(it.comment.id) ?: throw NotFoundException("Comment ${it.comment.id} not found")
            ac.assert { canView(comment, citizenOrNull) }

            call.respond(
                HttpStatusCode.OK,
                comment.toOutput()
            )
        }
    }
}
