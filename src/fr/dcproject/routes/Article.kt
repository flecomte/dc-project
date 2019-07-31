package fr.dcproject.routes

import Paths
import fr.postgresjson.serializer.serialize
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.repository.Article as ArticleRepository

@KtorExperimentalLocationsAPI
fun Route.article(repo: ArticleRepository) {
    get<Paths.ArticlesRequest> {
        call.respondText("todo")
    }
    get<Paths.ArticleRequest> {
        call.respondText(it.article.serialize())
    }
    post<Paths.PostArticleRequest>() {
        val article = call.receive<ArticleEntity>()
        repo.upsert(article)
        call.respond(article)
    }
}