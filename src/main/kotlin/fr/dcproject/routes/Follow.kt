package fr.dcproject.routes

import Paths
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.User
import fr.dcproject.repository.FollowArticleRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.delete
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import org.joda.time.DateTime
import java.util.*
import fr.dcproject.entity.Follow as FollowEntity

// TODO get current citizen
val currentCitizen = Citizen(
    id = UUID.fromString("64b7b379-2298-43ec-b428-ba134930cabd"),
    name = Citizen.Name("todo", "todo"),
    birthday = DateTime.now(),
    user = User(username = "plop", plainPassword = "plip")
)

@KtorExperimentalLocationsAPI
fun Route.followArticle(repo: FollowArticleRepository) {
    post<Paths.ArticleFollowRequest> {
        repo.follow(FollowEntity(target = it.article, citizen = currentCitizen))
        call.respond(HttpStatusCode.Created)
    }

    delete<Paths.ArticleFollowRequest> {
        repo.unfollow(FollowEntity(target = it.article, citizen = currentCitizen))
        call.respond(HttpStatusCode.NoContent)
    }
}