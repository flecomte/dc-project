package fr.dcproject.component.citizen.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

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
