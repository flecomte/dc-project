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
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.entity.Follow as FollowEntity
import fr.dcproject.repository.FollowConstitution as FollowConstitutionRepository

// TODO get current citizen
val currentCitizen2 = Citizen(
    id = UUID.fromString("64b7b379-2298-43ec-b428-ba134930cabd"),
    name = Citizen.Name("todo", "todo"),
    birthday = DateTime.now(),
    user = User(username = "plop", plainPassword = "plip")
)

@KtorExperimentalLocationsAPI
object FollowConstitutionPaths {
    @Location("/constitutions/{constitution}/follow") class ConstitutionFollowRequest(val constitution: ConstitutionEntity)
    @Location("/citizens/{citizen}/follows/constitutions") class CitizenFollowConstitutionRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.followConstitution(repo: FollowConstitutionRepository) {
    post<FollowConstitutionPaths.ConstitutionFollowRequest> {
        repo.follow(FollowEntity(target = it.constitution, citizen = currentCitizen2))
        call.respond(HttpStatusCode.Created)
    }

    delete<FollowConstitutionPaths.ConstitutionFollowRequest> {
        repo.unfollow(FollowEntity(target = it.constitution, citizen = currentCitizen2))
        call.respond(HttpStatusCode.NoContent)
    }

    get<FollowConstitutionPaths.CitizenFollowConstitutionRequest> {
        val follows = repo.findByCitizen(it.citizen)
        call.respond(follows)
    }
}
