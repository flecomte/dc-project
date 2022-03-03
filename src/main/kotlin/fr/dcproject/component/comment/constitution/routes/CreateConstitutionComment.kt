package fr.dcproject.component.comment.constitution.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.comment.constitution.database.CommentConstitutionRepository
import fr.dcproject.component.comment.constitution.routes.CreateConstitutionComment.CreateConstitutionCommentRequest.Input
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentForUpdate
import fr.dcproject.component.comment.toOutput
import fr.dcproject.component.constitution.database.ConstitutionRef
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object CreateConstitutionComment {
    @Location("/constitutions/{constitution}/comments")
    class CreateConstitutionCommentRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
        class Input(val content: String) {
            fun validate() = Validation<Input> {
                Input::content {
                    minLength(20)
                    maxLength(6000)
                }
            }.validate(this)
        }
    }

    fun Route.createConstitutionComment(repo: CommentConstitutionRepository, ac: CommentAccessControl) {
        post<CreateConstitutionCommentRequest> {
            mustBeAuth()

            call.receiveOrBadRequest<Input>()
                .apply { validate().badRequestIfNotValid() }
                .run {
                    CommentForUpdate(
                        target = it.constitution,
                        createdBy = citizen,
                        content = content
                    )
                }.let { comment ->
                    ac.canCreate(comment, citizenOrNull).assert()
                    repo.comment(comment)

                    call.respond(
                        HttpStatusCode.Created,
                        comment.toOutput()
                    )
                }
        }
    }
}
