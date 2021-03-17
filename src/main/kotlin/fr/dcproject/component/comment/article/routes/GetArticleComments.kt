package fr.dcproject.component.comment.article.routes

import fr.dcproject.common.dto.toOutput
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
                        val createdAt: DateTime = comment.createdAt
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
                        val createdBy: Any = comment.createdBy.let { c ->
                            object {
                                val id: UUID = c.id
                                val name: Any = c.name.let { n ->
                                    object {
                                        val firstName: String = n.firstName
                                        val lastName: String = n.lastName
                                    }
                                }
                                val user: Any = c.user.let { u ->
                                    object {
                                        val username: String = u.username
                                    }
                                }
                            }
                        }
                        val votes: Any = object {
                            val up: Int = 0
                            val neutral: Int = 0
                            val down: Int = 0
                            val total: Int = 0
                            val score: Int = 0
                        }
                    }
                }
            )
        }
    }
}
