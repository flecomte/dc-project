package fr.dcproject.component.comment.article.routes

import fr.dcproject.component.comment.article.routes.CreateCommentArticle.createCommentArticle
import fr.dcproject.component.comment.article.routes.GetArticleComments.getArticleComments
import fr.dcproject.component.comment.article.routes.GetCitizenArticleComments.getCitizenArticleComments
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installCommentArticleRoutes() {
    authenticate(optional = true) {
        getArticleComments(get(), get())
        createCommentArticle(get(), get())
        getCitizenArticleComments(get(), get())
    }
}
