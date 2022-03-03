package fr.dcproject.component.opinion.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.opinion.OpinionChoiceAccessControl
import fr.dcproject.component.opinion.database.OpinionChoiceRef
import fr.dcproject.component.opinion.database.OpinionChoiceRepository
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
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
            ac.canView(it.opinionChoice, citizenOrNull).assert()

            call.respond(
                HttpStatusCode.OK,
                opinionChoice.toOutput()
            )
        }
    }
}
