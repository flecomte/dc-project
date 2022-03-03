package fr.dcproject.component.comment.article.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.article.database.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.toOutput
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
object GetArticleComments {
    @Location("/articles/{article}/comments")
    class ArticleCommentsRequest(
        article: UUID,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null,
        val sort: String = "createdAt"
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        val article = ArticleRef(article)

        fun validate() = Validation<ArticleCommentsRequest> {
            ArticleCommentsRequest::page {
                minimum(1)
            }
            ArticleCommentsRequest::limit {
                minimum(1)
                maximum(50)
            }
            ArticleCommentsRequest::sort ifPresent {
                enum(
                    "votes",
                    "createdAt",
                )
            }
        }.validate(this)
    }

    fun Route.getArticleComments(repo: CommentArticleRepository, ac: CommentAccessControl) {
        get<ArticleCommentsRequest> {
            it.validate().badRequestIfNotValid()

            val comments = repo.findByTarget(it.article, it.page, it.limit, it.sort)
            if (comments.result.isNotEmpty()) {
                ac.canView(comments.result, citizenOrNull).assert()
            }
            call.respond(
                HttpStatusCode.OK,
                comments.toOutput { comment ->
                    comment.toOutput()
                }
            )
        }
    }
}
