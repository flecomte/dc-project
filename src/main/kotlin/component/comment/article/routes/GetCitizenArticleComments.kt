package fr.dcproject.component.comment.article.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

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
