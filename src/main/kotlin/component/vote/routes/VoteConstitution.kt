package fr.dcproject.component.vote.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.constitution.ConstitutionRef
import fr.dcproject.component.constitution.ConstitutionRepository
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.VoteConstitutionRepository
import fr.dcproject.component.vote.entity.VoteForUpdate
import fr.dcproject.component.vote.routes.VoteConstitution.ConstitutionVoteRequest.Input
import fr.dcproject.security.assert
import fr.dcproject.utils.receiveOrBadRequest
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object VoteConstitution {
    @Location("/constitutions/{constitution}/vote")
    class ConstitutionVoteRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
        data class Input(var note: Int)
    }

    fun Route.voteConstitution(repo: VoteConstitutionRepository, ac: VoteAccessControl, constitutionRepo: ConstitutionRepository) {
        put<ConstitutionVoteRequest> {
            val constitution = constitutionRepo.findById(it.constitution.id) ?: throw NotFoundException("Unable to find constitution ${it.constitution.id}")
            val content = call.receiveOrBadRequest<Input>()
            val vote = VoteForUpdate(
                target = constitution,
                note = content.note,
                createdBy = this.citizen
            )
            ac.assert { canCreate(vote, citizenOrNull) }
            repo.vote(vote)
            call.respond(HttpStatusCode.Created)
        }
    }
}
