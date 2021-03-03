package fr.dcproject.component.constitution.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.constitution.ConstitutionAccessControl
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.dcproject.component.constitution.database.ConstitutionRepository
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetConstitution {
    @Location("/constitutions/{constitution}")
    class GetConstitutionRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
    }

    fun Route.getConstitution(ac: ConstitutionAccessControl, constitutionRepo: ConstitutionRepository) {
        get<GetConstitutionRequest> {
            val constitution = constitutionRepo.findById(it.constitution.id) ?: throw NotFoundException("Unable to find constitution ${it.constitution.id}")
            ac.assert { canView(constitution, citizenOrNull) }
            call.respond(constitution)
        }
    }
}
