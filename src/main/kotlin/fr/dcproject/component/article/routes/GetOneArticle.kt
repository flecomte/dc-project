package fr.dcproject.component.article.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.ArticleViewRepository
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.auth.citizenOrNull
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.launch
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetOneArticle {
    @Location("/articles/{article}")
    class ArticleRequest(article: UUID) {
        val article = ArticleRef(article)
    }

    fun Route.getOneArticle(viewRepository: ArticleViewRepository<ArticleForView>, ac: ArticleAccessControl, repo: ArticleRepository) {
        get<ArticleRequest> {
            val article: ArticleForView = repo.findById(it.article.id) ?: throw NotFoundException("Article ${it.article.id} not found")
            ac.assert { canView(article, citizenOrNull) }

            call.respond(
                article.let { a ->
                    object {
                        val id = a.id
                        val versionId = a.versionId
                        val versionNumber = a.versionNumber
                        val title = a.title
                        val anonymous = a.anonymous
                        val content = a.content
                        val description = a.description
                        val tags = a.tags
                        val draft = a.draft
                        val lastVersion = a.lastVersion
                        val createdAt = a.createdAt
                        val createdBy: Any = object {
                            val id: UUID = a.createdBy.id
                            val name: Any = object {
                                val firstName: String = a.createdBy.name.firstName
                                val lastName: String = a.createdBy.name.lastName
                            }
                            val email: String = a.createdBy.email
                        }
                        val workgroup: Any? = a.workgroup?.let { w ->
                            object {
                                val id: UUID = w.id
                                val name: String = w.name
                            }
                        }
                        val votes: Any = object {
                            val up: Int = a.votes.up
                            val neutral: Int = a.votes.neutral
                            val down: Int = a.votes.down
                            val total: Int = a.votes.total
                            val score: Int = a.votes.score
                        }
                        val views: Any = viewRepository.getViewsCount(article).let { v ->
                            object {
                                val total = v.total
                                val unique = v.unique
                            }
                        }
                        val opinions: Map<String, Int> = a.opinions
                    }
                }
            )

            launch {
                viewRepository.addView(call.request.local.remoteHost, article, citizenOrNull)
            }
        }
    }
}
