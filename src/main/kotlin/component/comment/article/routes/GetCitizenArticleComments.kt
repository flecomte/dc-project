package fr.dcproject.component.comment.article.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetCitizenArticleComments {
    @Location("/citizens/{citizen}/comments/articles")
    class CitizenCommentArticleRequest(citizen: UUID) {
        val citizen = CitizenRef(citizen)
    }

    fun Route.getCitizenArticleComments(repo: CommentArticleRepository, ac: CommentAccessControl) {
        get<CitizenCommentArticleRequest> {
            repo.findByCitizen(it.citizen).let { comments ->
                ac.assert { canView(comments.result, citizenOrNull) }
                call.respond(comments)
            }
        }
    }
}
