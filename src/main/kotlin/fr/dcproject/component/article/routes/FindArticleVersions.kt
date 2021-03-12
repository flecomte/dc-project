package fr.dcproject.component.article.routes

import fr.dcproject.common.dto.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.database.ArticleForListing
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.auth.citizenOrNull
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object FindArticleVersions {
    @Location("/articles/{article}/versions")
    class ArticleVersionsRequest(
        article: UUID,
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
        val article = ArticleRef(article)
    }

    private fun ArticleRepository.findVersions(request: ArticleVersionsRequest) =
        findVersionsById(request.page, request.limit, request.article.id)

    fun Route.findArticleVersions(repo: ArticleRepository, ac: ArticleAccessControl) {
        get<ArticleVersionsRequest> {
            repo.findVersions(it)
                .apply { ac.assert { canView(result, citizenOrNull) } }
                .run {
                    call.respond(
                        toOutput { a: ArticleForListing ->
                            object {
                                val id = a.id
                                val title = a.title
                                val createdBy = object {
                                    val id = a.createdBy.id
                                    val name = a.createdBy.name.let { n ->
                                        object {
                                            val firstName = n.firstName
                                            val lastName = n.lastName
                                        }
                                    }
                                    val email = a.createdBy.email
                                }
                                val workgroup = a.workgroup?.let { w ->
                                    object {
                                        val id = w.id
                                        val name = w.name
                                    }
                                }
                                val draft = a.draft
                                val lastVersion = a.lastVersion
                            }
                        }
                    )
                }
        }
    }
}
