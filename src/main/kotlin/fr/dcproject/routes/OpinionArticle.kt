package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.CitizenRef
import fr.dcproject.entity.OpinionChoiceRef
import fr.dcproject.entity.request.RequestBuilder
import fr.dcproject.entity.request.getContent
import fr.dcproject.security.voter.OpinionVoter.Action.CREATE
import fr.dcproject.security.voter.OpinionVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import fr.dcproject.utils.toUUID
import io.ktor.application.ApplicationCall
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
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.repository.OpinionArticle as OpinionArticleRepository

@KtorExperimentalLocationsAPI
object OpinionArticlePaths {
    /**
     * Get paginated opinions of citizen for all articles
     */
    @Location("/citizens/{citizen}/opinions/articles")
    class CitizenOpinionArticleRequest(
        val citizen: CitizenRef,
        page: Int = 1,
        limit: Int = 50
    ) : PaginatedRequestI by PaginatedRequest(page, limit)

    /**
     * Put an opinion on one article
     */
    @Location("/articles/{article}/opinions")
    @KtorExperimentalAPI
    class ArticleOpinion(val article: ArticleEntity) : RequestBuilder<List<OpinionChoiceRef>> {

        private class Content(ids: List<String>) : KoinComponent {
            val ids = ids.map { it.toUUID() }
        }

        override suspend fun getContent(call: ApplicationCall): List<OpinionChoiceRef> =
            call.receive<Content>().ids.map { OpinionChoiceRef(it) }
    }

    /**
     * Get all Opinion of citizen on targets by target ids
     */
    @Location("/citizens/{citizen}/opinions")
    class CitizenOpinions(val citizen: CitizenEntity, id: List<String>) : KoinComponent {
        val id: List<UUID> = id.toUUID()
        val opinionsEntities = get<OpinionArticleRepository>()
            .findCitizenOpinionsByTargets(citizen, this.id)
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
        call.getContent(it)
            .let { choices ->
                assertCan(CREATE, it.article)
                repo.updateOpinions(choices, citizen, it.article)
            }.let {
                call.respond(HttpStatusCode.Created, it)
            }
    }
}
