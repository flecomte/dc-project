package fr.dcproject.component.follow.routes.article

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.follow.FollowArticleRepository
import fr.dcproject.component.follow.FollowVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object GetFollowArticle {
    @Location("/articles/{article}/follows")
    class ArticleFollowRequest(val article: ArticleRef)

    fun Route.getFollowArticle(repo: FollowArticleRepository, voter: FollowVoter) {
        get<ArticleFollowRequest> {
            repo.findFollow(citizen, it.article)?.let { follow ->
                voter.assert { canView(follow, citizenOrNull) }
                call.respond(follow)
            } ?: call.respond(HttpStatusCode.NoContent)
        }
    }
}
