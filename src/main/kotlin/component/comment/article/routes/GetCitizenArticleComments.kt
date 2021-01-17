package fr.dcproject.component.comment.article.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
@Location("/citizens/{citizen}/comments/articles")
class CitizenCommentArticleRequest(val citizen: Citizen)

@KtorExperimentalLocationsAPI
fun Route.getCitizenArticleComments(repo: CommentArticleRepository, voter: CommentVoter) {
    get<CitizenCommentArticleRequest> {
        repo.findByCitizen(it.citizen).let { comments ->
            voter.assert { canView(comments.result, citizenOrNull) }
            call.respond(comments)
        }
    }
}