package fr.dcproject.component.article.routes

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.article.ArticleVoter
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.voter.assert
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
@Location("/articles/{article}/versions")
class ArticleVersionsRequest(
    val article: ArticleForView,
    page: Int = 1,
    limit: Int = 50,
    val sort: String? = null,
    val direction: RepositoryI.Direction? = null,
    val search: String? = null
) {
    val page: Int = if (page < 1) 1 else page
    val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
}

@KtorExperimentalLocationsAPI
private fun ArticleRepository.findVersions(request: ArticleVersionsRequest) =
    findVersionsByVersionId(request.page, request.limit, request.article.versionId)

@KtorExperimentalLocationsAPI
fun Route.findArticleVersions(repo: ArticleRepository, voter: ArticleVoter) {
    get<ArticleVersionsRequest> {
        repo.findVersions(it)
            .apply { voter.assert { canView(it.article, citizenOrNull) } }
            .let { call.respond(it) }
    }
}
