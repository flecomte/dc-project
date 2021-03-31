package fr.dcproject.component.vote.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.database.VoteArticleRepository
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
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
    }

    fun Route.getCitizenVotesOnArticle(repo: VoteArticleRepository, ac: VoteAccessControl) {
        get<CitizenVoteArticleRequest> {
            mustBeAuth()
            val votes = repo.findByCitizen(it.citizen, it.page, it.limit)
            ac.assert { canView(votes.result, citizenOrNull) }

            call.respond(
                HttpStatusCode.OK,
                votes.toOutput { it.toOutput() }
            )
        }
    }
}
