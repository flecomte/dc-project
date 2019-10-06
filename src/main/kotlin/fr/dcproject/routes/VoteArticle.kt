package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.Citizen
import fr.dcproject.repository.CommentArticle
import fr.dcproject.repository.VoteArticleComment
import fr.dcproject.routes.VoteArticlePaths.ArticleCommentVoteRequest
import fr.dcproject.routes.VoteArticlePaths.ArticleVoteRequest
import fr.dcproject.security.voter.VoteVoter.Action.CREATE
import fr.dcproject.security.voter.VoteVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Vote as VoteEntity
import fr.dcproject.repository.VoteArticle as VoteArticleRepository

@KtorExperimentalLocationsAPI
object VoteArticlePaths {
    @Location("/articles/{article}/vote")
    class ArticleVoteRequest(val article: ArticleEntity) {
        data class Content(var note: Int)
    }

    @Location("/articles/{article}/comments/{comment}/vote")
    class ArticleCommentVoteRequest(val article: ArticleEntity, val comment: UUID) {
        data class Content(var note: Int)
    }

    @Location("/citizens/{citizen}/votes/articles")
    class CitizenVoteArticleRequest(
        val citizen: Citizen,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null
    ): PaginatedRequestI by PaginatedRequest(page, limit)

    @Location("/citizens/{citizen}/votes")
    class CitizenVotesByIdsRequest(val citizen: Citizen, id: List<String>) {
        val id: List<UUID> = id
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { UUID.fromString(it) }
    }
}

@KtorExperimentalLocationsAPI
fun Route.voteArticle(repo: VoteArticleRepository, voteCommentRepo: VoteArticleComment, commentRepo: CommentArticle) {
    put<ArticleVoteRequest> {
        val content = call.receive<ArticleVoteRequest.Content>()
        val vote = VoteEntity(
            target = it.article,
            note = content.note,
            createdBy = this.citizen
        )
        assertCan(CREATE, vote)
        val votes = repo.vote(vote)
        call.respond(HttpStatusCode.Created, votes)
    }

    put<ArticleCommentVoteRequest> {
        val comment = commentRepo.findById(it.comment)!!
        val content = call.receive<ArticleCommentVoteRequest.Content>()
        val vote = VoteEntity(
            target = comment,
            note = content.note,
            createdBy = this.citizen
        )
        assertCan(CREATE, vote)
        val votes = voteCommentRepo.vote(vote)
        call.respond(HttpStatusCode.Created, votes)
    }

    get<VoteArticlePaths.CitizenVoteArticleRequest> {
        val votes = repo.findByCitizen(it.citizen, it.page, it.limit)
        assertCan(VIEW, votes.result)

        call.respond(votes)
    }

    get<VoteArticlePaths.CitizenVotesByIdsRequest> {
        val votes = repo.findCitizenVotesByTargets(it.citizen, it.id)
        assertCan(VIEW, votes)

        call.respond(votes)
    }
}