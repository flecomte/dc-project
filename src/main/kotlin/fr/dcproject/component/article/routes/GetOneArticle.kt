package fr.dcproject.component.article.routes

import fr.dcproject.common.dto.CreatedAt
import fr.dcproject.common.dto.Versionable
import fr.dcproject.common.security.assert
import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.article.ArticleViewManager
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.opinion.dto.Opinionable
import fr.dcproject.component.views.dto.Viewable
import fr.dcproject.component.views.entity.ViewAggregation
import fr.dcproject.component.vote.dto.Votable
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

    class Output(
        article: ArticleForView,
        views: ViewAggregation = ViewAggregation()
    ) : CreatedAt by CreatedAt.Imp(article),
        Opinionable by Opinionable.Imp(article),
        Votable by Votable.Imp(article),
        Versionable by Versionable.Imp(article),
        Viewable by Viewable.Imp(views) {
        val id = article.id
        val title = article.title
        val anonymous = article.anonymous
        val content = article.content
        val description = article.description
        val tags = article.tags
        val draft = article.draft
        val lastVersion = article.lastVersion
        val createdBy = article.createdBy
        val workgroup = article.workgroup?.let { Workgroup(article.workgroup.id, article.workgroup.name) }

        class Workgroup(val id: UUID, val name: String)
    }

    fun Route.getOneArticle(viewManager: ArticleViewManager<ArticleForView>, ac: ArticleAccessControl, repo: ArticleRepository) {
        get<ArticleRequest> {
            val article: ArticleForView = repo.findById(it.article.id) ?: throw NotFoundException("Article ${it.article.id} not found")
            ac.assert { canView(article, citizenOrNull) }

            Output(
                article,
                viewManager.getViewsCount(article)
            ).also { out ->
                call.respond(out)
            }

            launch {
                viewManager.addView(call.request.local.remoteHost, article, citizenOrNull)
            }
        }
    }
}
