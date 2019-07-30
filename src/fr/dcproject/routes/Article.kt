package fr.dcproject.routes

import Paths
import fr.postgresjson.serializer.serialize
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respondText
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
fun Route.article() {
    get<Paths.ArticlesRequest> {
        call.respondText("todo")
    }
    get<Paths.ArticleRequest> {
        call.respondText(it.article.serialize())
    }
}