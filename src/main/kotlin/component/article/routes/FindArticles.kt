package fr.dcproject.component.article.routes

import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.ArticleForListing
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.dcproject.security.assert
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
                .let { call.respond(it) }
        }
    }
}
