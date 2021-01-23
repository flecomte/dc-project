package fr.dcproject.component.constitution.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.constitution.ConstitutionAccessControl
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.component.constitution.Constitution as ConstitutionEntity

@KtorExperimentalLocationsAPI
object GetConstitution {
    @Location("/constitutions/{constitution}")
    class GetConstitutionRequest(val constitution: ConstitutionEntity)

    fun Route.getConstitution(ac: ConstitutionAccessControl) {
        get<GetConstitutionRequest> {
            ac.assert { canView(it.constitution, citizenOrNull) }
            call.respond(it.constitution)
        }
    }
}
