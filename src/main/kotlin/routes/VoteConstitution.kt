package fr.dcproject.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.entity.VoteForUpdate
import fr.dcproject.routes.VoteConstitutionPaths.ConstitutionVoteRequest.Content
import fr.dcproject.security.voter.VoteVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.repository.VoteConstitution as VoteConstitutionRepository

@KtorExperimentalLocationsAPI
object VoteConstitutionPaths {
    @Location("/constitutions/{constitution}/vote")
    class ConstitutionVoteRequest(val constitution: ConstitutionEntity) {
        data class Content(var note: Int)
    }

    @Location("/citizens/{citizen}/votes/constitutions")
    class CitizenVoteConstitutionRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.voteConstitution(repo: VoteConstitutionRepository, voter: VoteVoter) {
    put<VoteConstitutionPaths.ConstitutionVoteRequest> {
        val content = call.receive<Content>()
        val vote = VoteForUpdate(
            target = it.constitution,
            note = content.note,
            createdBy = this.citizen
        )
        voter.assert { canCreate(vote, citizenOrNull) }
        repo.vote(vote)
        call.respond(HttpStatusCode.Created)
    }
}
