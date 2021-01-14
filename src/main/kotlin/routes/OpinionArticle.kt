package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.entity.OpinionChoiceRef
import fr.dcproject.security.voter.OpinionVoter.Action.CREATE
import fr.dcproject.security.voter.OpinionVoter.Action.VIEW
import fr.dcproject.utils.toUUID
import fr.ktorVoter.assertCan
import fr.ktorVoter.assertCanAll
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import org.koin.core.KoinComponent
import org.koin.core.get
import java.util.*
import fr.dcproject.component.citizen.Citizen as CitizenEntity
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
    class ArticleOpinion(val article: ArticleForView) {
        class Body(ids: List<String>) {
            val ids = ids.map { OpinionChoiceRef(it.toUUID()) }
        }
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
        assertCanAll(VIEW, it.opinionsEntities)

        call.respond(it.opinionsEntities)
    }

    put<OpinionArticlePaths.ArticleOpinion> {
        call.receive<OpinionArticlePaths.ArticleOpinion.Body>().ids.let { choices ->
            assertCan(CREATE, it.article)
            repo.updateOpinions(choices, citizen, it.article)
        }.let {
            call.respond(HttpStatusCode.Created, it)
        }
    }
}
