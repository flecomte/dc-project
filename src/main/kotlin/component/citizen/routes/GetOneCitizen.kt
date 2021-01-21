package fr.dcproject.component.citizen.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object GetOneCitizen {
    @Location("/citizens/{citizen}")
    class CitizenRequest(val citizen: Citizen)

    fun Route.getOneCitizen(voter: CitizenVoter) {
        get<CitizenRequest> {
            voter.assert { canView(it.citizen, citizenOrNull) }

            call.respond(it.citizen)
        }
    }
}
