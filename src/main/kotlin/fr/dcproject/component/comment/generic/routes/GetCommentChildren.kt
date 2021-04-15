package fr.dcproject.component.comment.generic.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentRef
import fr.dcproject.component.comment.generic.database.CommentRepository
import fr.dcproject.component.comment.toOutput
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetCommentChildren {
    @Location("/comments/{comment}/children")
    class CommentChildrenRequest(
        comment: UUID,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        val comment = CommentRef(comment)
    }

    fun Route.getChildrenComments(repo: CommentRepository, ac: CommentAccessControl) {
        get<CommentChildrenRequest> {
            val comments =
                repo.findByParent(
                    it.comment,
                    it.page,
                    it.limit
                )

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
