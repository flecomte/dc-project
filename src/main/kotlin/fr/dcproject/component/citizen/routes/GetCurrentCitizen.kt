package fr.dcproject.component.citizen.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenAccessControl
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object GetCurrentCitizen {
    @Location("/citizens/current")
    class CurrentCitizenRequest

    fun Route.getCurrentCitizen(ac: CitizenAccessControl) {
        get<CurrentCitizenRequest> {
            val currentUser = citizenOrNull
            if (currentUser === null) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                ac.assert { canView(currentUser, citizenOrNull) }
                call.respond(citizen)
            }
        }
    }
}
