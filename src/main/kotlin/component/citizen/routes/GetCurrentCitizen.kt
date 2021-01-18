package fr.dcproject.component.citizen.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
@Location("/citizens/current")
class CurrentCitizenRequest

@KtorExperimentalLocationsAPI
fun Route.getCurrentCitizen(voter: CitizenVoter) {
    get<CurrentCitizenRequest> {
        val currentUser = citizenOrNull
        if (currentUser === null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            voter.assert { canView(currentUser, citizenOrNull) }
            call.respond(citizen)
        }
    }
}
