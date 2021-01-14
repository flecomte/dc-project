package fr.dcproject.component.comment.generic.routes

import fr.dcproject.component.comment.generic.CommentRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.ktorVoter.assertCanAll
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/comments/{comment}/children")
class CommentChildrenRequest(
    val comment: UUID,
    page: Int = 1,
    limit: Int = 50,
    val search: String? = null
) {
    val page: Int = if (page < 1) 1 else page
    val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.getChildrenComments(repo: CommentRepository) {
    get<CommentChildrenRequest> {
        val comments =
            repo.findByParent(
                it.comment,
                it.page,
                it.limit
            )

        assertCanAll(CommentVoter.Action.VIEW, comments.result)

        call.respond(HttpStatusCode.OK, comments)
    }
}