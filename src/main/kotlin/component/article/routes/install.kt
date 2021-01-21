package fr.dcproject.component.article.routes

import fr.dcproject.component.article.routes.FindArticleVersions.findArticleVersions
import fr.dcproject.component.article.routes.FindArticles.findArticles
import fr.dcproject.component.article.routes.GetOneArticle.getOneArticle
import fr.dcproject.component.article.routes.UpsertArticle.upsertArticle
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installArticleRoutes() {
    authenticate(optional = true) {
        findArticles(get(), get())
        findArticleVersions(get(), get())
        getOneArticle(get(), get())
        upsertArticle(get(), get(), get())
    }
}
