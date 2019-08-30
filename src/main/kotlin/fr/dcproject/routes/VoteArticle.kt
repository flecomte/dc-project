package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.Citizen
import fr.dcproject.routes.VoteArticlePaths.ArticleVoteRequest.Content
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Vote as VoteEntity
import fr.dcproject.repository.VoteArticle as VoteArticleRepository

@KtorExperimentalLocationsAPI
object VoteArticlePaths {
    @Location("/articles/{article}/vote") class ArticleVoteRequest(val article: ArticleEntity) {
        data class Content(var note: Int)
    }
    @Location("/citizens/{citizen}/votes/articles") class CitizenVoteArticleRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.voteArticle(repo: VoteArticleRepository) {
    put<VoteArticlePaths.ArticleVoteRequest> {
        val content = call.receive<Content>()
        repo.vote(VoteEntity(
            target = it.article,
            note = content.note,
            createdBy = this.citizen
        ))
        call.respond(HttpStatusCode.Created)
    }
}