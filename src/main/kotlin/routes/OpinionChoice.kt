package fr.dcproject.routes

import fr.dcproject.entity.OpinionChoice
import fr.dcproject.security.voter.OpinionChoiceVoter.Action.VIEW
import fr.ktorVoter.assertCan
import fr.ktorVoter.assertCanAll
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import fr.dcproject.repository.OpinionChoice as OpinionChoiceRepository

@KtorExperimentalLocationsAPI
object OpinionChoicePaths {
    @Location("/opinions/{opinionChoice}")
    class OpinionChoiceRequest(val opinionChoice: OpinionChoice)

    @Location("/opinions")
    class OpinionChoicesRequest(val targets: List<String> = emptyList())
}

@KtorExperimentalLocationsAPI
fun Route.opinionChoice(repo: OpinionChoiceRepository) {
    get<OpinionChoicePaths.OpinionChoiceRequest> {
        assertCan(VIEW, it.opinionChoice)

        call.respond(it.opinionChoice)
    }

    get<OpinionChoicePaths.OpinionChoicesRequest> {
        val opinions = repo.findOpinionsChoices(it.targets)
        assertCanAll(VIEW, opinions)

        call.respond(opinions)
    }
}