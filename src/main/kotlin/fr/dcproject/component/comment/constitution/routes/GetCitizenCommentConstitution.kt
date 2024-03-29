package fr.dcproject.component.comment.constitution.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.comment.constitution.database.CommentConstitutionRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.toOutput
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetCitizenCommentConstitution {
    @Location("/citizens/{citizen}/comments/constitutions")
    class GetCitizenCommentConstitutionRequest(citizen: UUID) {
        val citizen = CitizenRef(citizen)
    }

    fun Route.getCitizenCommentConstitution(repo: CommentConstitutionRepository, ac: CommentAccessControl) {
        get<GetCitizenCommentConstitutionRequest> {
            mustBeAuth()
            val comments = repo.findByCitizen(it.citizen)
            ac.assert { canView(comments.result, citizenOrNull) }
            call.respond(
                HttpStatusCode.OK,
                comments.toOutput { comment ->
                    comment.toOutput()
                }
            )
        }
    }
}
