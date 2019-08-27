package fr.dcproject.routes

import fr.dcproject.entity.Citizen
import fr.dcproject.entity.User
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.Route
import org.joda.time.DateTime
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Follow as FollowEntity
import fr.dcproject.repository.FollowArticle as FollowArticleRepository

// TODO get current citizen
val currentCitizen = Citizen(
    id = UUID.fromString("64b7b379-2298-43ec-b428-ba134930cabd"),
    name = Citizen.Name("todo", "todo"),
    birthday = DateTime.now(),
    user = User(username = "plop", plainPassword = "plip")
)

@KtorExperimentalLocationsAPI
object FollowArticlePaths {
    @Location("/articles/{article}/follow") class ArticleFollowRequest(val article: ArticleEntity)
    @Location("/citizens/{citizen}/follows/articles") class CitizenFollowArticleRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.followArticle(repo: FollowArticleRepository) {
    post<FollowArticlePaths.ArticleFollowRequest> {
        repo.follow(FollowEntity(target = it.article, createdBy = currentCitizen))
        call.respond(HttpStatusCode.Created)
    }

    delete<FollowArticlePaths.ArticleFollowRequest> {
        repo.unfollow(FollowEntity(target = it.article, createdBy = currentCitizen))
        call.respond(HttpStatusCode.NoContent)
    }

    get<FollowArticlePaths.CitizenFollowArticleRequest> {
        val follows = repo.findByCitizen(it.citizen)
        call.respond(follows)
    }
}