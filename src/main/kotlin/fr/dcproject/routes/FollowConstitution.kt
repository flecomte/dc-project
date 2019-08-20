package fr.dcproject.routes

import Paths
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.User
import fr.dcproject.repository.FollowConstitutionRepository
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
val currentCitizen2 = Citizen(
    id = UUID.fromString("64b7b379-2298-43ec-b428-ba134930cabd"),
    name = Citizen.Name("todo", "todo"),
    birthday = DateTime.now(),
    user = User(username = "plop", plainPassword = "plip")
)

@KtorExperimentalLocationsAPI
fun Route.followConstitution(repo: FollowConstitutionRepository) {
    post<Paths.ConstitutionFollowRequest> {
        repo.follow(FollowEntity(target = it.constitution, citizen = currentCitizen2))
        call.respond(HttpStatusCode.Created)
    }

    delete<Paths.ConstitutionFollowRequest> {
        repo.unfollow(FollowEntity(target = it.constitution, citizen = currentCitizen2))
        call.respond(HttpStatusCode.NoContent)
    }
}
