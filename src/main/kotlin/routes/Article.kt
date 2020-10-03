package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.citizenOrNull
import fr.dcproject.entity.ArticleForUpdate
import fr.dcproject.entity.CitizenRef
import fr.dcproject.entity.WorkgroupRef
import fr.dcproject.entity.WorkgroupSimple
import fr.dcproject.event.ArticleUpdate
import fr.dcproject.event.raiseEvent
import fr.dcproject.repository.Article.Filter
import fr.dcproject.repository.Workgroup as WorkgroupRepository
import fr.dcproject.security.voter.ArticleVoter.Action.CREATE
import fr.dcproject.security.voter.ArticleVoter.Action.UPDATE
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
import org.koin.core.KoinComponent
import org.koin.core.inject
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
        val createdBy: String? = null,
        val workgroup: String? = null
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
    class PostArticleRequest : KoinComponent {
        class Article(
            val id: UUID?,
            val title: String,
            val anonymous: Boolean = true,
            val content: String,
            val description: String,
            val tags: List<String> = emptyList(),
            val draft: Boolean = false,
            val versionId: UUID?,
            val workgroup: WorkgroupRef? = null
        )

        private val workgroupRepository: WorkgroupRepository by inject()

        suspend fun getNewArticle(call: ApplicationCall): ArticleForUpdate = call.receive<Article>().run {
            ArticleForUpdate(
                id = id ?: UUID.randomUUID(),
                title = title,
                anonymous = anonymous,
                content = content,
                description = description,
                tags = tags,
                draft = draft,
                createdBy = call.citizen,
                workgroup = if (workgroup != null) workgroupRepository.findById(workgroup.id) as WorkgroupSimple<CitizenRef> else null,
                versionId = versionId
            )
        }
    }
}

@KtorExperimentalLocationsAPI
fun Route.article(repo: ArticleRepository, viewManager: ArticleViewManager) {
    get<ArticlesPaths.ArticlesRequest> {
        val articles = repo.find(
            it.page,
            it.limit,
            it.sort,
            it.direction,
            it.search,
            Filter(createdById = it.createdBy, workgroupId = it.workgroup)
        )
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
            if(article.isNew) {
                assertCan(CREATE, article)
            } else {
                assertCan(UPDATE, article)
            }
            val newArticle = repo.upsert(article) ?: error("Article not updated")
            call.respond(article)
            raiseEvent(ArticleUpdate.event, ArticleUpdate(newArticle))
        }
    }
}
