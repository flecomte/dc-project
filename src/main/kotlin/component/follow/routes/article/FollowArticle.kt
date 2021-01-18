package fr.dcproject.component.follow.routes.article

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.follow.FollowArticleRepository
import fr.dcproject.component.follow.FollowForUpdate
import fr.dcproject.component.follow.FollowVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object FollowArticle {
    @Location("/articles/{article}/follows")
    class ArticleFollowRequest(val article: ArticleRef)

    fun Route.followArticle(repo: FollowArticleRepository, voter: FollowVoter) {
        post<ArticleFollowRequest> {
            val follow = FollowForUpdate(target = it.article, createdBy = this.citizen)
            voter.assert { canCreate(follow, citizenOrNull) }
            repo.follow(follow)
            call.respond(HttpStatusCode.Created)
        }
    }
}
