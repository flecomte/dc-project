package fr.dcproject.component.vote.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.database.VoteArticleRepository
import fr.dcproject.component.vote.database.VoteForUpdate
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minimum
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object PutVoteOnArticle {
    @Location("/articles/{article}/vote")
    class ArticleVoteRequest(article: UUID) {
        val article = ArticleRef(article)
        data class Input(var note: Int) {
            fun validate() = Validation<Input> {
                Input::note {
                    minimum(-1)
                    maximum(1)
                }
            }.validate(this)
        }
    }

    fun Route.putVoteOnArticle(repo: VoteArticleRepository, ac: VoteAccessControl, articleRepo: ArticleRepository) {
        put<ArticleVoteRequest> {
            mustBeAuth()

            val input = call.receiveOrBadRequest<ArticleVoteRequest.Input>()
                .apply { validate().badRequestIfNotValid() }
            val article = articleRepo.findById(it.article.id) ?: throw NotFoundException("Article ${it.article.id} not found")
            val vote = VoteForUpdate(
                target = article,
                note = input.note,
                createdBy = this.citizen
            )
            ac.canCreate(vote, citizenOrNull).assert()
            val votes = repo.vote(vote)
            call.respond(
                HttpStatusCode.Created,
                votes.toOutput()
            )
        }
    }
}
