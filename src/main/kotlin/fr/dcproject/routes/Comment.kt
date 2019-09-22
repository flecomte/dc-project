package fr.dcproject.routes

import fr.dcproject.security.voter.CommentVoter.Action.UPDATE
import fr.dcproject.security.voter.CommentVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.*
import fr.dcproject.repository.CommentGeneric as CommentRepository

@KtorExperimentalLocationsAPI
object CommentPaths {
    // TODO: change UUID by entity converter
    @Location("/comments/{comment}") class CommentRequest(val comment: UUID)

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
}

@KtorExperimentalLocationsAPI
fun Route.comment(repo: CommentRepository) {
    get<CommentPaths.CommentRequest> {
        val comment = repo.findById(it.comment)!!
        assertCan(VIEW, comment)

        call.respond(HttpStatusCode.OK, comment)
    }

    get<CommentPaths.CommentChildrenRequest> {
        val comments =
            repo.findByParent(
                it.comment,
                it.page,
                it.limit
            )

        assertCan(VIEW, comments.result)

        call.respond(HttpStatusCode.OK, comments)
    }

    put<CommentPaths.CommentRequest> {
        val comment = repo.findById(it.comment)!!
        assertCan(UPDATE,comment)

        comment.content = call.receiveText()
        repo.edit(comment)

        call.respond(HttpStatusCode.OK, comment)
    }
}