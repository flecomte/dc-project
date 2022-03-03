package fr.dcproject.component.vote.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.dcproject.component.constitution.database.ConstitutionRepository
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.database.VoteConstitutionRepository
import fr.dcproject.component.vote.database.VoteForUpdate
import fr.dcproject.component.vote.routes.PutVoteOnConstitution.ConstitutionVoteRequest.Input
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minimum
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
object PutVoteOnConstitution {
    @Location("/constitutions/{constitution}/vote")
    class ConstitutionVoteRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
        data class Input(var note: Int) {
            fun validate() = Validation<Input> {
                Input::note {
                    minimum(-1)
                    maximum(1)
                }
            }.validate(this)
        }
    }

    fun Route.voteConstitution(repo: VoteConstitutionRepository, ac: VoteAccessControl, constitutionRepo: ConstitutionRepository) {
        put<ConstitutionVoteRequest> {
            mustBeAuth()
            val constitution = constitutionRepo.findById(it.constitution.id) ?: throw NotFoundException("Unable to find constitution ${it.constitution.id}")
            val input = call.receiveOrBadRequest<Input>()
                .apply { validate().badRequestIfNotValid() }
            val vote = VoteForUpdate(
                target = constitution,
                note = input.note,
                createdBy = this.citizen
            )
            ac.canCreate(vote, citizenOrNull).assert()
            repo.vote(vote)
            call.respond(HttpStatusCode.Created)
        }
    }
}
