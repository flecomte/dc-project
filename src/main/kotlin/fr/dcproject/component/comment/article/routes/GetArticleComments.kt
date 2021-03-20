package fr.dcproject.component.comment.article.routes

import fr.dcproject.common.dto.toOutput
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.article.database.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.joda.time.DateTime
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetArticleComments {
    @Location("/articles/{article}/comments")
    class ArticleCommentsRequest(
        article: UUID,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null,
        sort: String = CommentArticleRepository.Sort.CREATED_AT.sql
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        val article = ArticleRef(article)
        val sort: CommentArticleRepository.Sort = CommentArticleRepository.Sort.fromString(sort) ?: CommentArticleRepository.Sort.CREATED_AT
    }

    fun Route.getArticleComments(repo: CommentArticleRepository, ac: CommentAccessControl) {
        get<ArticleCommentsRequest> {
            val comments = repo.findByTarget(it.article, it.page, it.limit, it.sort)
            if (comments.result.isNotEmpty()) {
                ac.assert { canView(comments.result, citizenOrNull) }
            }
            call.respond(
                HttpStatusCode.OK,
                comments.toOutput { comment ->
                    object {
                        val id: UUID = comment.id
                        val content: String = comment.content
                        val childrenCount: Int = comment.childrenCount ?: 0
                        val createdAt: DateTime = comment.createdAt
                        val updatedAt: DateTime = comment.updatedAt
                        val parent: Any? = comment.parent?.let { p ->
                            object {
                                val id: UUID = p.id
                                val reference: String = p.reference
                            }
                        }
                        val target: Any = comment.target.let { t ->
                            object {
                                val id: UUID = t.id
                                val reference: String = t.reference
                            }
                        }
                        val createdBy: Any = comment.createdBy.toOutput()
                        val votes: Any = comment.votes.let { v ->
                            object {
                                val up: Int = v.up
                                val neutral: Int = v.neutral
                                val down: Int = v.down
                                val total: Int = v.total
                                val score: Int = v.score
                            }
                        }
                    }
                }
            )
        }
    }
}
