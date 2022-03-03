package fr.dcproject.component.vote.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.database.VoteArticleRepository
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minimum
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetCitizenVotesOnArticle {
    @Location("/citizens/{citizen}/votes/articles")
    class CitizenVoteArticleRequest(
        citizen: UUID,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        val citizen = CitizenRef(citizen)
        fun validate() = Validation<CitizenVoteArticleRequest> {
            CitizenVoteArticleRequest::page {
                minimum(1)
            }
            CitizenVoteArticleRequest::limit {
                minimum(1)
                maximum(50)
            }
        }.validate(this)
    }

    fun Route.getCitizenVotesOnArticle(repo: VoteArticleRepository, ac: VoteAccessControl) {
        get<CitizenVoteArticleRequest> {
            mustBeAuth()
            it.validate().badRequestIfNotValid()

            val votes = repo.findByCitizen(it.citizen, it.page, it.limit)
            ac.canView(votes.result, citizenOrNull).assert()

            call.respond(
                HttpStatusCode.OK,
                votes.toOutput { it.toOutput() }
            )
        }
    }
}
