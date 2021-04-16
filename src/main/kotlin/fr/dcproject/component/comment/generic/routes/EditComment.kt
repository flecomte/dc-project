package fr.dcproject.component.comment.generic.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentForUpdate
import fr.dcproject.component.comment.generic.database.CommentRef
import fr.dcproject.component.comment.generic.database.CommentRepository
import fr.dcproject.component.comment.toOutput
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
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
object EditComment {
    @Location("/comments/{comment}")
    class EditCommentRequest(comment: UUID) {
        val comment = CommentRef(comment)
        class Input(val content: String) {
            fun validate() = Validation<Input> {
                Input::content {
                    minLength(20)
                    maxLength(6000)
                }
            }.validate(this)
        }
    }

    fun Route.editComment(repo: CommentRepository, ac: CommentAccessControl) {
        put<EditCommentRequest> {
            mustBeAuth()
            val commentOld = repo.findById(it.comment.id) ?: throw NotFoundException("Comment not found")
            ac.assert { canUpdate(commentOld, citizenOrNull) }

            call.receiveOrBadRequest<EditCommentRequest.Input>()
                .apply { validate().badRequestIfNotValid() }
                .run {
                    CommentForUpdate(
                        id = commentOld.id,
                        createdBy = commentOld.createdBy,
                        target = commentOld.target,
                        parent = commentOld.parent,
                        content = content,
                    )
                }
                .let { repo.edit(it) }
                .let {
                    call.respond(
                        HttpStatusCode.OK,
                        it.toOutput()
                    )
                }
        }
    }
}
