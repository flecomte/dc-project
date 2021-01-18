package fr.dcproject.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.entity.OpinionChoice
import fr.dcproject.security.voter.OpinionChoiceVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.repository.OpinionChoice as OpinionChoiceRepository

@KtorExperimentalLocationsAPI
object OpinionChoicePaths {
    @Location("/opinions/{opinionChoice}")
    class OpinionChoiceRequest(val opinionChoice: OpinionChoice)

    @Location("/opinions")
    class OpinionChoicesRequest(val targets: List<String> = emptyList())
}

@KtorExperimentalLocationsAPI
fun Route.opinionChoice(repo: OpinionChoiceRepository, voter: OpinionChoiceVoter) {
    get<OpinionChoicePaths.OpinionChoiceRequest> {
        voter.assert { canView(it.opinionChoice, citizenOrNull) }

        call.respond(it.opinionChoice)
    }

    get<OpinionChoicePaths.OpinionChoicesRequest> {
        val opinionChoices = repo.findOpinionsChoices(it.targets)
        voter.assert { canView(opinionChoices, citizenOrNull) }

        call.respond(opinionChoices)
    }
}
