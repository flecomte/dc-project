package fr.dcproject.component.comment.constitution.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.comment.constitution.CommentConstitutionRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object GetCitizenCommentConstitution {
    @Location("/citizens/{citizen}/comments/constitutions")
    class GetCitizenCommentConstitutionRequest(val citizen: Citizen)

    fun Route.getCitizenCommentConstitution(repo: CommentConstitutionRepository, ac: CommentAccessControl) {
        get<GetCitizenCommentConstitutionRequest> {
            val comments = repo.findByCitizen(it.citizen)
            ac.assert { canView(comments.result, citizenOrNull) }
            call.respond(comments)
        }
    }
}
