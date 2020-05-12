package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.citizenOrNull
import fr.dcproject.event.ArticleUpdate
import fr.dcproject.event.raiseEvent
import fr.dcproject.repository.Article.Filter
import fr.dcproject.security.voter.ArticleVoter.Action.CREATE
import fr.dcproject.security.voter.ArticleVoter.Action.VIEW
import fr.dcproject.views.ArticleViewManager
import fr.ktorVoter.assertCan
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.launch
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity
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
    class PostArticleRequest {
        class Article(
            val id: UUID?,
            val title: String,
            val anonymous: Boolean = true,
            val content: String,
            val description: String,
            val tags: List<String> = emptyList(),
            val draft: Boolean = false,
            val versionId: UUID?
        )

        suspend fun getNewArticle(call: ApplicationCall): ArticleEntity = call.receive<Article>().run {
            ArticleEntity(
                id ?: UUID.randomUUID(),
                title,
                anonymous,
                content,
                description,
                tags,
                draft,
                createdBy = call.citizen
            ).also {
                it.versionId = versionId ?: UUID.randomUUID()
            }
        }
    }
}

@KtorExperimentalLocationsAPI
fun Route.article(repo: ArticleRepository, viewManager: ArticleViewManager) {
    get<ArticlesPaths.ArticlesRequest> {
        val articles =
            repo.find(it.page, it.limit, it.sort, it.direction, it.search, Filter(createdById = it.createdBy))
        assertCan(VIEW, articles.result)
        call.respond(articles)
    }

    get<ArticlesPaths.ArticleRequest> {
        assertCan(VIEW, it.article)

        it.article.views = viewManager.getViewsCount(it.article)

        call.respond(it.article)

        launch {
            viewManager.addView(call.request.local.remoteHost, it.article, citizenOrNull)
        }
    }

    get<ArticlesPaths.ArticleVersionsRequest> {
        assertCan(VIEW, it.article)

        repo.findVerionsByVersionsId(it.page, it.limit, it.article.versionId).let {
            call.respond(it)
        }
    }

    post<ArticlesPaths.PostArticleRequest> {
        it.getNewArticle(call).also { article ->
            assertCan(CREATE, article)
            repo.upsert(article)
            call.respond(article)
            raiseEvent(ArticleUpdate.event, ArticleUpdate(article))
        }
    }
}
