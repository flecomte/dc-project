package fr.dcproject.component.comment.generic.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizen
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
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object CreateComment {
    @Location("/comments/{comment}")
    class CreateCommentRequest(comment: UUID) {
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

    fun Route.createCommentChildren(repo: CommentRepository, ac: CommentAccessControl) {
        post<CreateCommentRequest> {
            mustBeAuth()

            call.receiveOrBadRequest<CreateCommentRequest.Input>()
                .apply { validate().badRequestIfNotValid() }
                .run {
                    val parent = repo.findById(it.comment.id) ?: throw NotFoundException("Comment not found")
                    CommentForUpdate(
                        content = content,
                        createdBy = citizen,
                        target = parent.target,
                        parent = parent,
                    )
                }.let { newComment ->
                    ac.assert { canCreate(newComment, citizenOrNull) }
                    repo.comment(newComment)
                    call.respond(HttpStatusCode.Created, newComment.toOutput())
                }
        }
    }
}
