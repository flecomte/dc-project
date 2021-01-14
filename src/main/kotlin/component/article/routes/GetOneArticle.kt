package fr.dcproject.component.article.routes

import fr.dcproject.citizenOrNull
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.article.ArticleViewManager
import fr.dcproject.component.article.ArticleVoter
import fr.dcproject.component.article.routes.ArticleRequest.Output
import fr.dcproject.dto.*
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/articles/{articleId}")
class ArticleRequest(val articleId: UUID) : KoinComponent {
    val repo: ArticleRepository by inject()

    @KtorExperimentalAPI
    val article: ArticleForView = repo.findById(articleId) ?: throw NotFoundException("Article $articleId not found")

    class Output(
        article: ArticleForView,
        views: fr.dcproject.entity.ViewAggregation = fr.dcproject.entity.ViewAggregation()
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
        val workgroup = article.workgroup // TODO change to workgroup DTO
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.getOneArticle(viewManager: ArticleViewManager, voter: ArticleVoter) {
    get<ArticleRequest> {
        voter.assert { canView(it.article, citizenOrNull) }

        Output(
            it.article,
            viewManager.getViewsCount(it.article)
        ).also { out ->
            call.respond(out)
        }

        launch {
            viewManager.addView(call.request.local.remoteHost, it.article, citizenOrNull)
        }
    }
}