package fr.dcproject.component.vote.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.comment.generic.database.CommentRef
import fr.dcproject.component.comment.generic.database.CommentRepository
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.database.VoteCommentRepository
import fr.dcproject.component.vote.database.VoteForUpdate
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minimum
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
object PutVoteOnComment {
    @Location("/comments/{comment}/vote")
    class CommentVoteRequest(comment: UUID) {
        val comment = CommentRef(comment)
        data class Input(var note: Int) {
            fun validate() = Validation<Input> {
                Input::note {
                    minimum(-1)
                    maximum(1)
                }
            }.validate(this)
        }
    }

    fun Route.putVoteOnComment(voteCommentRepo: VoteCommentRepository, commentRepo: CommentRepository, ac: VoteAccessControl) {
        put<CommentVoteRequest> {
            mustBeAuth()

            val comment = commentRepo.findById(it.comment.id) ?: throw NotFoundException("Comment ${it.comment.id} not found")
            val input = call.receiveOrBadRequest<CommentVoteRequest.Input>()
                .apply { validate().badRequestIfNotValid() }

            val vote = VoteForUpdate(
                target = comment,
                note = input.note,
                createdBy = this.citizen
            )
            ac.assert { canCreate(vote, citizenOrNull) }
            val votes = voteCommentRepo.vote(vote)
            call.respond(
                HttpStatusCode.Created,
                votes.toOutput()
            )
        }
    }
}
