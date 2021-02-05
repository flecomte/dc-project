package fr.dcproject.component.opinion.routes

import fr.dcproject.common.entity.TargetRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenBasicI
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.opinion.OpinionAccessControl
import fr.dcproject.component.opinion.entity.Opinion
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.dcproject.security.assert
import fr.postgresjson.connexion.Paginated
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.component.opinion.OpinionRepositoryArticle as OpinionArticleRepository
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetMyOpinionsArticle {
    /**
     * Get paginated opinions of citizen for all articles
     */
    @Location("/citizens/{citizen}/opinions/articles")
    class CitizenOpinionsArticleRequest(
        citizen: UUID,
        page: Int = 1,
        limit: Int = 50
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        val citizen = CitizenRef(citizen)
    }

    fun Route.getMyOpinionsArticle(repo: OpinionArticleRepository, ac: OpinionAccessControl) {
        get<CitizenOpinionsArticleRequest> {
            val opinions: Paginated<Opinion<TargetRef>> = repo.findCitizenOpinions(citizen, it.page, it.limit)
            ac.assert { canView(opinions.result, citizenOrNull) }
            call.respond(opinions)
        }
    }
}
