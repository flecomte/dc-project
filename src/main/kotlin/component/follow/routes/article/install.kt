package fr.dcproject.component.follow.routes.article

import fr.dcproject.component.follow.routes.article.FollowArticle.followArticle
import fr.dcproject.component.follow.routes.article.GetFollowArticle.getFollowArticle
import fr.dcproject.component.follow.routes.article.GetMyFollowsArticle.getMyFollowsArticle
import fr.dcproject.component.follow.routes.article.UnfollowArticle.unfollowArticle
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installFollowArticleRoutes() {
    authenticate(optional = true) {
        followArticle(get(), get())
        unfollowArticle(get(), get())
        getFollowArticle(get(), get())
        getMyFollowsArticle(get(), get())
    }
}
