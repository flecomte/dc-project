package fr.dcproject.component.vote.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.database.VoteRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
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
            mustBeAuth()
            val votes = repo.findCitizenVotesByTargets(it.citizen, it.id)
            if (votes.isNotEmpty()) {
                ac.assert { canView(votes, citizenOrNull) }
            }
            call.respond(
                HttpStatusCode.OK,
                votes.map { it.toOutput() }
            )
        }
    }
}
