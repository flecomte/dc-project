package fr.dcproject.component.opinion.routes

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.opinion.OpinionVoter
import fr.dcproject.component.opinion.entity.Opinion
import fr.dcproject.utils.toUUID
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.koin.core.KoinComponent
import java.util.UUID
import fr.dcproject.component.citizen.Citizen as CitizenEntity
import fr.dcproject.component.opinion.OpinionRepositoryArticle as OpinionArticleRepository

@KtorExperimentalLocationsAPI
object GetCitizenOpinions {
    /**
     * Get all Opinion of citizen on targets by target ids
     */
    @Location("/citizens/{citizen}/opinions")
    class CitizenOpinions(val citizen: CitizenEntity, id: List<String>) : KoinComponent {
        val id: List<UUID> = id.toUUID()
    }

    fun Route.getCitizenOpinions(repo: OpinionArticleRepository, voter: OpinionVoter) {
        get<CitizenOpinions> {
            val opinionsEntities: List<Opinion<ArticleRef>> = repo.findCitizenOpinionsByTargets(it.citizen, it.id)
            voter.assert { canView(opinionsEntities, citizenOrNull) }

            call.respond(opinionsEntities)
        }
    }
}
