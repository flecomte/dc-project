package fr.dcproject.component.opinion.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.opinion.OpinionChoiceRepository
import fr.dcproject.component.opinion.OpinionChoiceVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object GetOpinionChoices {
    @Location("/opinions")
    class OpinionChoicesRequest(val targets: List<String> = emptyList())

    fun Route.getOpinionChoices(repo: OpinionChoiceRepository, voter: OpinionChoiceVoter) {
        get<OpinionChoicesRequest> {
            val opinionChoices = repo.findOpinionsChoices(it.targets)
            voter.assert { canView(opinionChoices, citizenOrNull) }

            call.respond(opinionChoices)
        }
    }
}
