package fr.dcproject.component.comment.generic.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.generic.CommentRef
import fr.dcproject.component.comment.generic.CommentRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalLocationsAPI
@Location("/comments/{comment}")
class CommentRequest(val comment: CommentRef)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.getOneComment(repo: CommentRepository, voter: CommentVoter) {
    get<CommentRequest> {
        val comment = repo.findById(it.comment.id) ?: throw NotFoundException("Comment ${it.comment.id} not found")
        voter.assert { canView(comment, citizenOrNull) }

        call.respond(HttpStatusCode.OK, comment)
    }
}
