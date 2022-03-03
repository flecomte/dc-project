package fr.dcproject.component.follow.routes.citizen

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.database.FollowCitizenRepository
import fr.dcproject.component.follow.database.FollowForUpdate
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object FollowCitizen {
    @Location("/citizens/{citizen}/follows")
    class CitizenFollowRequest(citizen: UUID) {
        val citizen = CitizenRef(citizen)
    }

    fun Route.followCitizen(repo: FollowCitizenRepository, ac: FollowAccessControl) {
        post<CitizenFollowRequest> {
            mustBeAuth()
            val follow = FollowForUpdate(target = it.citizen, createdBy = this.citizen)
            ac.canCreate(follow, citizenOrNull).assert()
            repo.follow(follow)
            call.respond(HttpStatusCode.Created)
        }
    }
}
