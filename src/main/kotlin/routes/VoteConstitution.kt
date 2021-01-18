package fr.dcproject.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.entity.VoteForUpdate
import fr.dcproject.routes.VoteConstitutionPaths.ConstitutionVoteRequest.Content
import fr.dcproject.security.voter.VoteVoter
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
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
