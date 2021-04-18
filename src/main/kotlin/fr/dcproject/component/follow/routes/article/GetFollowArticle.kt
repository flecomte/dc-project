package fr.dcproject.component.follow.routes.article

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.database.FollowArticleRepository
import fr.dcproject.component.follow.routes.citizen.toOutput
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetFollowArticle {
    @Location("/articles/{article}/follows")
    class ArticleFollowRequest(article: UUID) {
        val article = ArticleRef(article)
    }

    fun Route.getFollowArticle(repo: FollowArticleRepository, ac: FollowAccessControl) {
        get<ArticleFollowRequest> {
            repo.findFollow(citizen, it.article)?.let { follow ->
                ac.assert { canView(follow, citizenOrNull) }
                call.respond(
                    HttpStatusCode.OK,
                    follow.toOutput()
                )
            } ?: call.respond(HttpStatusCode.NoContent)
        }
    }
}
