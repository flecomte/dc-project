package fr.dcproject.component.follow.routes.constitution

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.FollowConstitutionRepository
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetMyFollowsConstitution {
    @Location("/citizens/{citizen}/follows/constitutions")
    class CitizenFollowConstitutionRequest(citizen: UUID) {
        val citizen = CitizenRef(citizen)
    }

    fun Route.getMyFollowsConstitution(repo: FollowConstitutionRepository, ac: FollowAccessControl) {
        get<CitizenFollowConstitutionRequest> {
            val follows = repo.findByCitizen(it.citizen)
            ac.assert { canView(follows.result, citizenOrNull) }
            call.respond(follows)
        }
    }
}
