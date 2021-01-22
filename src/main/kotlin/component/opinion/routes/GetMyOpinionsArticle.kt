package fr.dcproject.component.opinion.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.opinion.OpinionAccessControl
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.component.opinion.OpinionRepositoryArticle as OpinionArticleRepository

@KtorExperimentalLocationsAPI
object GetMyOpinionsArticle {
    /**
     * Get paginated opinions of citizen for all articles
     */
    @Location("/citizens/{citizen}/opinions/articles")
    class CitizenOpinionsArticleRequest(
        val citizen: CitizenRef,
        page: Int = 1,
        limit: Int = 50
    ) : PaginatedRequestI by PaginatedRequest(page, limit)

    fun Route.getMyOpinionsArticle(repo: OpinionArticleRepository, ac: OpinionAccessControl) {
        get<CitizenOpinionsArticleRequest> {
            val opinions = repo.findCitizenOpinions(citizen, it.page, it.limit)
            ac.assert { canView(opinions.result, citizenOrNull) }
            call.respond(opinions)
        }
    }
}
