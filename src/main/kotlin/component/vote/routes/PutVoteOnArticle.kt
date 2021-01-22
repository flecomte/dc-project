package fr.dcproject.component.vote.routes

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.VoteArticleRepository
import fr.dcproject.component.vote.entity.VoteForUpdate
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object PutVoteOnArticle {
    @Location("/articles/{article}/vote")
    class ArticleVoteRequest(val article: ArticleForView) {
        data class Content(var note: Int)
    }

    fun Route.putVoteOnArticle(repo: VoteArticleRepository, ac: VoteAccessControl) {
        put<ArticleVoteRequest> {
            val content = call.receive<ArticleVoteRequest.Content>()
            val vote = VoteForUpdate(
                target = it.article,
                note = content.note,
                createdBy = this.citizen
            )
            ac.assert { canCreate(vote, citizenOrNull) }
            val votes = repo.vote(vote)
            call.respond(HttpStatusCode.Created, votes)
        }
    }
}
