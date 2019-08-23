package fr.dcproject.routes

import Paths
import fr.dcproject.security.voter.ArticleVoter
import fr.dcproject.security.voter.assertCan
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.repository.Article as ArticleRepository

@KtorExperimentalLocationsAPI
fun Route.article(repo: ArticleRepository) {
    get<Paths.ArticlesRequest> {
        val articles = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        call.respond(articles)
    }

    get<Paths.ArticleRequest> {
        call.respond(it.article)
    }

    post<Paths.PostArticleRequest> {
        call.assertCan(ArticleVoter.Action.CREATE)
        val article = call.receive<ArticleEntity>()
        repo.upsert(article)
        call.respond(article)
    }
}
