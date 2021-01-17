package fr.dcproject.component.citizen.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
@Location("/citizens/{citizen}")
class CitizenRequest(val citizen: Citizen)

@KtorExperimentalLocationsAPI
fun Route.getOneCitizen(voter: CitizenVoter) {
    get<CitizenRequest> {
        voter.assert { canView(it.citizen, citizenOrNull) }

        call.respond(it.citizen)
    }
}