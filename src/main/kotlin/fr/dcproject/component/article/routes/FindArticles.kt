package fr.dcproject.component.article.routes

import fr.dcproject.common.dto.toOutput
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.database.ArticleForListing
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object FindArticles {
    @Location("/articles")
    class ArticlesRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null,
        val createdBy: String? = null,
        val workgroup: String? = null
    ) : PaginatedRequestI by PaginatedRequest(page, limit)

    private fun ArticleRepository.findArticles(request: ArticlesRequest): Paginated<ArticleForListing> {
        return find(
            request.page,
            request.limit,
            request.sort,
            request.direction,
            request.search,
            ArticleRepository.Filter(createdById = request.createdBy, workgroupId = request.workgroup)
        )
    }

    fun Route.findArticles(repo: ArticleRepository, ac: ArticleAccessControl) {
        get<ArticlesRequest> {
            repo.findArticles(it)
                .apply { ac.assert { canView(result, citizenOrNull) } }
                .let {
                    call.respond(
                        it.toOutput {
                            object {
                                val id = it.id
                                val title = it.title
                                val createdBy: Any = it.createdBy.toOutput()
                                val workgroup = it.workgroup?.let {
                                    object {
                                        val id = it.id
                                        val name = it.name
                                    }
                                }
                                val draft = it.draft
                            }
                        }
                    )
                }
        }
    }
}
