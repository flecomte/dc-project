package fr.dcproject.component.opinion.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.opinion.OpinionChoiceAccessControl
import fr.dcproject.component.opinion.OpinionChoiceRepository
import fr.dcproject.component.opinion.entity.OpinionChoiceRef
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetOpinionChoice {
    @Location("/opinions/{opinionChoice}")
    class OpinionChoiceRequest(opinionChoice: UUID) {
        val opinionChoice = OpinionChoiceRef(opinionChoice)
    }

    fun Route.getOpinionChoice(ac: OpinionChoiceAccessControl, opinionChoiceRepository: OpinionChoiceRepository) {
        get<OpinionChoiceRequest> {
            val opinionChoice = opinionChoiceRepository.findOpinionChoiceById(it.opinionChoice.id) ?: throw NotFoundException("OpinionChoice ${it.opinionChoice.id} not found")
            ac.assert { canView(it.opinionChoice, citizenOrNull) }

            call.respond(opinionChoice)
        }
    }
}
