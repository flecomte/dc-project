package fr.dcproject.component.opinion.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.opinion.OpinionAccessControl
import fr.dcproject.component.opinion.entity.OpinionChoiceRef
import fr.dcproject.component.opinion.entity.OpinionForUpdate
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID
import fr.dcproject.component.opinion.OpinionRepositoryArticle as OpinionArticleRepository

@KtorExperimentalLocationsAPI
object OpinionArticle {
    /**
     * Put an opinion on one article
     */
    @Location("/articles/{article}/opinions")
    class ArticleOpinion(article: UUID) {
        val article = ArticleRef(article)
        class Body(ids: List<String>) {
            val ids: List<UUID> = ids.map { it.toUUID() }
        }
    }

    fun Route.setOpinionOnArticle(repo: OpinionArticleRepository, ac: OpinionAccessControl) {
        put<ArticleOpinion> {
            call.receiveOrBadRequest<ArticleOpinion.Body>().ids.map { id ->
                OpinionForUpdate(
                    choice = OpinionChoiceRef(id),
                    target = it.article,
                    createdBy = citizen
                )
            }.let { opinions ->
                ac.assert { canCreate(opinions, citizenOrNull) }
                repo.updateOpinions(opinions)
            }.let {
                call.respond(HttpStatusCode.Created, it)
            }
        }
    }
}
