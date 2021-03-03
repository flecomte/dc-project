package fr.dcproject.component.opinion.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.opinion.OpinionAccessControl
import fr.dcproject.component.opinion.database.Opinion
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID
import fr.dcproject.component.opinion.database.OpinionRepositoryArticle as OpinionArticleRepository

@KtorExperimentalLocationsAPI
object GetCitizenOpinions {
    /**
     * Get all Opinion of citizen on targets by target ids
     */
    @Location("/citizens/{citizen}/opinions")
    class CitizenOpinions(citizen: UUID, id: List<String>) {
        val citizen = CitizenRef(citizen)
        val id: List<UUID> = id.toUUID()
    }

    fun Route.getCitizenOpinions(repo: OpinionArticleRepository, ac: OpinionAccessControl) {
        get<CitizenOpinions> {
            val opinionsEntities: List<Opinion<ArticleRef>> = repo.findCitizenOpinionsByTargets(it.citizen, it.id)
            ac.assert { canView(opinionsEntities, citizenOrNull) }

            call.respond(opinionsEntities)
        }
    }
}
