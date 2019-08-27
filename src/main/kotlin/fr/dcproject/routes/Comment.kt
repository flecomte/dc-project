package fr.dcproject.routes

import fr.dcproject.security.voter.CommentVoter.Action.UPDATE
import fr.dcproject.security.voter.CommentVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import fr.postgresjson.entity.UuidEntity
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.*
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.repository.CommentGeneric as CommentRepository

typealias CommentEntityGeneric = CommentEntity<UuidEntity>
@KtorExperimentalLocationsAPI
object CommentPaths {
    // TODO: change UUID by entity converter
    @Location("/comments/{comment}") class CommentRequest(val comment: UUID)
}

@KtorExperimentalLocationsAPI
fun Route.comment(repo: CommentRepository) {
    get<CommentPaths.CommentRequest> {
        val comment = repo.findById(it.comment)!!
        assertCan(VIEW, comment)

        call.respond(HttpStatusCode.OK, comment)
    }

    put<CommentPaths.CommentRequest> {
        val comment = repo.findById(it.comment)!!
        assertCan(UPDATE,comment)

        comment.content = call.receiveText()
        repo.edit(comment as fr.dcproject.entity.Comment<UuidEntity>)

        call.respond(HttpStatusCode.OK, comment)
    }
}