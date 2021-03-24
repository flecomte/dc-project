package fr.dcproject.component.vote.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.generic.database.CommentRepository
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.database.VoteCommentRepository
import fr.dcproject.component.vote.database.VoteForUpdate
import io.ktor.application.call
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
    class CommentVoteRequest(val comment: UUID) {
        data class Content(var note: Int)
    }

    fun Route.putVoteOnComment(voteCommentRepo: VoteCommentRepository, commentRepo: CommentRepository, ac: VoteAccessControl) {
        put<CommentVoteRequest> {
            val comment = commentRepo.findById(it.comment)!!
            val content = call.receiveOrBadRequest<CommentVoteRequest.Content>()
            val vote = VoteForUpdate(
                target = comment,
                note = content.note,
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
