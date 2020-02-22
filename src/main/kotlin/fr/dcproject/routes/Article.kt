package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.event.ArticleUpdate
import fr.dcproject.event.EntityEvent
import fr.dcproject.repository.Article.Filter
import fr.dcproject.security.voter.ArticleVoter.Action.CREATE
import fr.dcproject.security.voter.ArticleVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.request.Article as ArticleEntityRequest
import fr.dcproject.repository.Article as ArticleRepository

@KtorExperimentalLocationsAPI
object ArticlesPaths {
    @Location("/articles")
    class ArticlesRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null,
        val createdBy: String? = null
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }

    @Location("/articles/{article}")
    class ArticleRequest(val article: ArticleEntity)

    @Location("/articles/{article}/versions")
    class ArticleVersionsRequest(
        val article: ArticleEntity,
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }

    @Location("/articles")
    class PostArticleRequest
}

@KtorExperimentalLocationsAPI
fun Route.article(repo: ArticleRepository) {
    get<ArticlesPaths.ArticlesRequest> {
        val articles =
            repo.find(it.page, it.limit, it.sort, it.direction, it.search, Filter(createdById = it.createdBy))
        assertCan(VIEW, articles.result)
        call.respond(articles)
    }

    get<ArticlesPaths.ArticleRequest> {
        assertCan(VIEW, it.article)

        call.respond(it.article)
    }

    get<ArticlesPaths.ArticleVersionsRequest> {
        assertCan(VIEW, it.article)

        val versions = repo.findVerionsByVersionsId(it.page, it.limit, it.article.versionId)

        call.respond(versions)
    }

    post<ArticlesPaths.PostArticleRequest> {
        val request = call.receive<ArticleEntityRequest>()
        val article = request.create(citizen)

        assertCan(CREATE, article)

        repo.upsert(article)
        application.environment.monitor.raise(EntityEvent.Type.UPDATE_ARTICLE.event, ArticleUpdate(article))

        call.respond(article)
    }
}
