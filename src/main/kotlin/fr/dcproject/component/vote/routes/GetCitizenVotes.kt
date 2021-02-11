package fr.dcproject.component.vote.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.VoteRepository
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
    class CitizenVotesRequest(citizen: UUID, id: List<String>) {
        val citizen = CitizenRef(citizen)
        val id: List<UUID> = id.toUUID()
    }

    fun Route.getCitizenVote(repo: VoteRepository, ac: VoteAccessControl) {
        get<CitizenVotesRequest> {
            val votes = repo.findCitizenVotesByTargets(it.citizen, it.id)
            if (votes.isNotEmpty()) {
                ac.assert { canView(votes, citizenOrNull) }
            }
            call.respond(votes)
        }
    }
}
