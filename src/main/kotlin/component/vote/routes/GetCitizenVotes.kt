package fr.dcproject.component.vote.routes

import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.vote.VoteRepository
import fr.dcproject.component.vote.VoteVoter
import fr.dcproject.utils.toUUID
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetCitizenVotes {
    @Location("/citizens/{citizen}/votes")
    class CitizenVotesRequest(val citizen: Citizen, id: List<String>) {
        val id: List<UUID> = id.toUUID()
    }

    fun Route.getCitizenVote(repo: VoteRepository, voter: VoteVoter) {
        get<CitizenVotesRequest> {
            val votes = repo.findCitizenVotesByTargets(it.citizen, it.id)
            if (votes.isNotEmpty()) {
                voter.assert { canView(votes, citizenOrNull) }
            }
            call.respond(votes)
        }
    }
}
