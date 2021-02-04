package fr.dcproject.component.follow.routes.article

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.FollowArticleRepository
import fr.dcproject.component.follow.FollowForUpdate
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object FollowArticle {
    @Location("/articles/{article}/follows")
    class ArticleFollowRequest(article: UUID) {
        val article = ArticleRef(article)
    }

    fun Route.followArticle(repo: FollowArticleRepository, ac: FollowAccessControl) {
        post<ArticleFollowRequest> {
            val follow = FollowForUpdate(target = it.article, createdBy = this.citizen)
            ac.assert { canCreate(follow, citizenOrNull) }
            repo.follow(follow)
            call.respond(HttpStatusCode.Created)
        }
    }
}
