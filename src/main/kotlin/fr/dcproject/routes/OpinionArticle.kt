package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.request.ArticleOpinionRequest
import fr.dcproject.security.voter.OpinionVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import fr.dcproject.utils.toUUID
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI
import org.koin.core.KoinComponent
import org.koin.core.get
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.repository.OpinionArticle as OpinionArticleRepository

@KtorExperimentalLocationsAPI
object OpinionArticlePaths {
    /**
     * Get paginated opinion of citizen for one article
     */
    @Location("/citizens/{citizen}/opinions/articles")
    class CitizenOpinionArticleRequest(
        val citizen: CitizenEntity,
        page: Int = 1,
        limit: Int = 50
    ) : PaginatedRequestI by PaginatedRequest(page, limit)

    /**
     * Put an opinion on one article
     */
    @Location("/articles/{article}/opinons")
    class ArticleOpinion(val article: ArticleEntity)

    /**
     * Get all Opinion of citizen on targets by target ids
     */
    @Location("/citizen/{citizen}/opinions")
    class CitizenOpinions(val citizen: CitizenEntity, id: List<String>): KoinComponent {
        val opinionsEntities = get<OpinionArticleRepository>()
            .findCitizenOpinionsByTargets(citizen, id.toUUID())
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.opinionArticle(repo: OpinionArticleRepository) {
    get<OpinionArticlePaths.CitizenOpinionArticleRequest> {
        val opinions = repo.findCitizenOpinions(citizen, it.page, it.limit)
        call.respond(opinions)
    }

    get<OpinionArticlePaths.CitizenOpinions> {
        assertCan(VIEW, it.opinionsEntities)

        call.respond(it.opinionsEntities)
    }

    put<OpinionArticlePaths.ArticleOpinion> {
        val optionArticle = call.receive<ArticleOpinionRequest>().create(citizen)
        assertCan(VIEW, optionArticle)

        call.respond(HttpStatusCode.Created, optionArticle)
    }
}
