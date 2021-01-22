package fr.dcproject.component.opinion.routes

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.opinion.OpinionVoter
import fr.dcproject.component.opinion.entity.OpinionChoiceRef
import fr.dcproject.component.opinion.entity.OpinionForUpdate
import fr.dcproject.utils.toUUID
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID
import fr.dcproject.repository.OpinionRepositoryArticle as OpinionArticleRepository

@KtorExperimentalLocationsAPI
object OpinionArticle {
    /**
     * Put an opinion on one article
     */
    @Location("/articles/{article}/opinions")
    class ArticleOpinion(val article: ArticleForView) {
        class Body(ids: List<String>) {
            val ids: List<UUID> = ids.map { it.toUUID() }
        }
    }

    fun Route.setOpinionOnArticle(repo: OpinionArticleRepository, voter: OpinionVoter) {
        put<ArticleOpinion> {
            call.receive<ArticleOpinion.Body>().ids.map { id ->
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
}
