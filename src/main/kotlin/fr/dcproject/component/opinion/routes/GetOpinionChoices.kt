package fr.dcproject.component.opinion.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.opinion.OpinionChoiceAccessControl
import fr.dcproject.component.opinion.database.OpinionChoiceRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object GetOpinionChoices {
    @Location("/opinions")
    class OpinionChoicesRequest(val targets: List<String> = emptyList())

    fun Route.getOpinionChoices(repo: OpinionChoiceRepository, ac: OpinionChoiceAccessControl) {
        get<OpinionChoicesRequest> {
            val opinionChoices = repo.findOpinionsChoices(it.targets)
            ac.assert { canView(opinionChoices, citizenOrNull) }

            call.respond(
                HttpStatusCode.OK,
                opinionChoices.map { it.toOutput() }
            )
        }
    }
}
