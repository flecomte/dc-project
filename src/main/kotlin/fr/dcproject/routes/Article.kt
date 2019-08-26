package fr.dcproject.routes

import fr.dcproject.security.voter.ArticleVoter.Action.CREATE
import fr.dcproject.security.voter.ArticleVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.repository.Article as ArticleRepository


@KtorExperimentalLocationsAPI
object ArticlesPaths {
    @Location("/articles") class ArticlesRequest(page: Int = 1, limit: Int = 50, val sort: String? = null, val direction: RepositoryI.Direction? = null, val search: String? = null) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }
    @Location("/articles/{article}") class ArticleRequest(val article: fr.dcproject.entity.Article)
    @Location("/articles/{article}/follow") class ArticleFollowRequest(val article: fr.dcproject.entity.Article)
    @Location("/articles") class PostArticleRequest
}

@KtorExperimentalLocationsAPI
fun Route.article(repo: ArticleRepository) {
    get<ArticlesPaths.ArticlesRequest> {
        assertCan(VIEW)

        val articles = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        call.respond(articles)
    }

    get<ArticlesPaths.ArticleRequest> {
        assertCan(VIEW, it.article)

        call.respond(it.article)
    }

    post<ArticlesPaths.PostArticleRequest> {
        assertCan(CREATE)

        val article = call.receive<ArticleEntity>()
        repo.upsert(article)
        call.respond(article)
    }
}
