package fr.dcproject.component.comment.constitution.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.constitution.database.CommentConstitutionRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.toOutput
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import io.konform.validation.Validation
import io.konform.validation.jsonschema.enum
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minimum
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
    class GetConstitutionCommentRequest(
        constitution: UUID,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null,
        val sort: String = "createdAt"
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        val constitution = ConstitutionRef(constitution)

        fun validate() = Validation<GetConstitutionCommentRequest> {
            GetConstitutionCommentRequest::page {
                minimum(1)
            }
            GetConstitutionCommentRequest::limit {
                minimum(1)
                maximum(50)
            }
            GetConstitutionCommentRequest::sort ifPresent {
                enum(
                    "votes",
                    "createdAt",
                )
            }
        }.validate(this)
    }

    fun Route.getConstitutionComment(repo: CommentConstitutionRepository, ac: CommentAccessControl) {
        get<GetConstitutionCommentRequest> {
            it.validate().badRequestIfNotValid()

            val comments = repo.findByTarget(it.constitution)
            ac.canView(comments.result, citizenOrNull).assert()
            call.respond(
                HttpStatusCode.OK,
                comments.toOutput { comment ->
                    comment.toOutput()
                }
            )
        }
    }
}
