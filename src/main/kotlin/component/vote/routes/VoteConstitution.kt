package fr.dcproject.component.vote.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.VoteConstitutionRepository
import fr.dcproject.component.vote.entity.VoteForUpdate
import fr.dcproject.component.vote.routes.VoteConstitution.ConstitutionVoteRequest.Input
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.component.constitution.Constitution as ConstitutionEntity

@KtorExperimentalLocationsAPI
object VoteConstitution {
    @Location("/constitutions/{constitution}/vote")
    class ConstitutionVoteRequest(val constitution: ConstitutionEntity) {
        data class Input(var note: Int)
    }

    fun Route.voteConstitution(repo: VoteConstitutionRepository, ac: VoteAccessControl) {
        put<ConstitutionVoteRequest> {
            val content = call.receive<Input>()
            val vote = VoteForUpdate(
                target = it.constitution,
                note = content.note,
                createdBy = this.citizen
            )
            ac.assert { canCreate(vote, citizenOrNull) }
            repo.vote(vote)
            call.respond(HttpStatusCode.Created)
        }
    }
}
