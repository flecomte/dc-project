package fr.dcproject.component.opinion.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.opinion.OpinionChoiceAccessControl
import fr.dcproject.component.opinion.entity.OpinionChoice
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object GetOpinionChoice {
    @Location("/opinions/{opinionChoice}")
    class OpinionChoiceRequest(val opinionChoice: OpinionChoice)

    fun Route.getOpinionChoice(ac: OpinionChoiceAccessControl) {
        get<OpinionChoiceRequest> {
            ac.assert { canView(it.opinionChoice, citizenOrNull) }

            call.respond(it.opinionChoice)
        }
    }
}
