package fr.dcproject.component.comment.constitution.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.constitution.CommentConstitutionRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.constitution.ConstitutionRef
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetConstitutionComment {
    @Location("/constitutions/{constitution}/comments")
    class GetConstitutionCommentRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
    }

    fun Route.getConstitutionComment(repo: CommentConstitutionRepository, ac: CommentAccessControl) {
        get<GetConstitutionCommentRequest> {
            val comments = repo.findByTarget(it.constitution)
            ac.assert { canView(comments.result, citizenOrNull) }
            call.respond(HttpStatusCode.OK, comments)
        }
    }
}
