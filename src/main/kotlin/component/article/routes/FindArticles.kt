package fr.dcproject.component.article.routes

import fr.dcproject.citizenOrNull
import fr.dcproject.component.article.ArticleForListing
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.article.ArticleVoter
import fr.dcproject.voter.assert
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@Location("/articles")
class ArticlesRequest(
    page: Int = 1,
    limit: Int = 50,
    val sort: String? = null,
    val direction: RepositoryI.Direction? = null,
    val search: String? = null,
    val createdBy: String? = null,
    val workgroup: String? = null
) {
    val page: Int = if (page < 1) 1 else page
    val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
}

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

fun Route.findArticles (repo: ArticleRepository, voter: ArticleVoter) {
    get<ArticlesRequest> {
        repo.findArticles(it)
            .apply { voter.assert { canView(result, citizenOrNull) } }
            .let { call.respond(it) }
    }
}
