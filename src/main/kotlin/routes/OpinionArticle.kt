package fr.dcproject.routes

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.entity.*
import fr.dcproject.security.voter.OpinionVoter
import fr.dcproject.utils.toUUID
import fr.dcproject.voter.assert
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
            val ids: List<UUID> = ids.map { it.toUUID() }
        }
    }

    /**
     * Get all Opinion of citizen on targets by target ids
     */
    @Location("/citizens/{citizen}/opinions")
    class CitizenOpinions(val citizen: CitizenEntity, id: List<String>) : KoinComponent {
        val id: List<UUID> = id.toUUID()
        val opinionsEntities: List<Opinion<ArticleRef>> = get<OpinionArticleRepository>()
            .findCitizenOpinionsByTargets(citizen, this.id)
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.opinionArticle(repo: OpinionArticleRepository, voter: OpinionVoter) {
    get<OpinionArticlePaths.CitizenOpinionArticleRequest> {
        val opinions = repo.findCitizenOpinions(citizen, it.page, it.limit)
        call.respond(opinions)
    }

    get<OpinionArticlePaths.CitizenOpinions> {
        voter.assert { canView(it.opinionsEntities, citizenOrNull) }

        call.respond(it.opinionsEntities)
    }

    put<OpinionArticlePaths.ArticleOpinion> {
        call.receive<OpinionArticlePaths.ArticleOpinion.Body>().ids.map { id ->
            OpinionForUpdate(
                choice = OpinionChoiceRef(id),
                target = it.article,
                createdBy = citizen
            )
        }.let { opinions ->
            voter.assert { canCreate(opinions, citizenOrNull) }
            repo.updateOpinions(opinions)
        }.let {
            call.respond(HttpStatusCode.Created, it)
        }
    }
}
