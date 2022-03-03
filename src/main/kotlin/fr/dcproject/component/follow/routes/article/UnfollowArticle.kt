package fr.dcproject.component.follow.routes.article

import fr.dcproject.common.security.assert
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.database.FollowArticleRepository
import fr.dcproject.component.follow.database.FollowForUpdate
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object UnfollowArticle {
    @Location("/articles/{article}/follows")
    class ArticleFollowRequest(article: UUID) {
        val article = ArticleRef(article)
    }

    fun Route.unfollowArticle(repo: FollowArticleRepository, ac: FollowAccessControl) {
        delete<ArticleFollowRequest> {
            mustBeAuth()
            val follow = FollowForUpdate(target = it.article, createdBy = this.citizen)
            ac.canDelete(follow, citizenOrNull).assert()
            repo.unfollow(follow)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
