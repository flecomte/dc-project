package fr.dcproject.component.comment.constitution.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.constitution.CommentConstitutionRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.constitution.ConstitutionRef
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object CreateConstitutionComment {
    @Location("/constitutions/{constitution}/comments")
    class CreateConstitutionCommentRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
    }

    fun Route.createConstitutionComment(repo: CommentConstitutionRepository, ac: CommentAccessControl) {
        post<CreateConstitutionCommentRequest> {
            val content = call.receiveText()
            val comment = CommentForUpdate(
                target = it.constitution,
                createdBy = citizen,
                content = content
            )
            ac.assert { canCreate(comment, citizenOrNull) }
            repo.comment(comment)

            call.respond(HttpStatusCode.Created, comment)
        }
    }
}
