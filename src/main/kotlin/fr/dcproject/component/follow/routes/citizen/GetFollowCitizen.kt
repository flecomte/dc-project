package fr.dcproject.component.follow.routes.citizen

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.database.FollowCitizenRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetFollowCitizen {
    @Location("/citizens/{citizen}/follows")
    class CitizenFollowRequest(citizen: UUID) {
        val citizen = CitizenRef(citizen)
    }

    fun Route.getFollowCitizen(repo: FollowCitizenRepository, ac: FollowAccessControl) {
        get<CitizenFollowRequest> {
            repo.findFollow(citizen, it.citizen)?.let { follow ->
                ac.canView(follow, citizenOrNull).assert()
                call.respond(
                    HttpStatusCode.OK,
                    follow.toOutput()
                )
            } ?: call.respond(HttpStatusCode.NoContent)
        }
    }
}
