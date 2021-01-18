package fr.dcproject.routes

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.comment.generic.CommentRepository
import fr.dcproject.entity.VoteForUpdate
import fr.dcproject.repository.VoteComment
import fr.dcproject.routes.VoteArticlePaths.ArticleVoteRequest
import fr.dcproject.routes.VoteArticlePaths.CommentVoteRequest
import fr.dcproject.security.voter.VoteVoter
import fr.dcproject.utils.toUUID
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID
import fr.dcproject.repository.VoteArticle as VoteArticleRepository

@KtorExperimentalLocationsAPI
object VoteArticlePaths {
    @Location("/articles/{article}/vote")
    class ArticleVoteRequest(val article: ArticleForView) {
        data class Content(var note: Int)
    }

    @Location("/comments/{comment}/vote")
    class CommentVoteRequest(val comment: UUID) {
        data class Content(var note: Int)
    }

    @Location("/citizens/{citizen}/votes/articles")
    class CitizenVoteArticleRequest(
        val citizen: Citizen,
        page: Int = 1,
        limit: Int = 50,
        val search: String? = null
    ) : PaginatedRequestI by PaginatedRequest(page, limit)

    @Location("/citizens/{citizen}/votes")
    class CitizenVotesByIdsRequest(val citizen: Citizen, id: List<String>) {
        val id: List<UUID> = id.toUUID()
    }
}

@KtorExperimentalLocationsAPI
fun Route.voteArticle(repo: VoteArticleRepository, voteCommentRepo: VoteComment, commentRepo: CommentRepository, voter: VoteVoter) {
    put<ArticleVoteRequest> {
        val content = call.receive<ArticleVoteRequest.Content>()
        val vote = VoteForUpdate(
            target = it.article,
            note = content.note,
            createdBy = this.citizen
        )
        voter.assert { canCreate(vote, citizenOrNull) }
        val votes = repo.vote(vote)
        call.respond(HttpStatusCode.Created, votes)
    }

    put<CommentVoteRequest> {
        val comment = commentRepo.findById(it.comment)!!
        val content = call.receive<CommentVoteRequest.Content>()
        val vote = VoteForUpdate(
            target = comment,
            note = content.note,
            createdBy = this.citizen
        )
        voter.assert { canCreate(vote, citizenOrNull) }
        val votes = voteCommentRepo.vote(vote)
        call.respond(HttpStatusCode.Created, votes)
    }

    get<VoteArticlePaths.CitizenVoteArticleRequest> {
        val votes = repo.findByCitizen(it.citizen, it.page, it.limit)
        voter.assert { canView(votes.result, citizenOrNull) }

        call.respond(votes)
    }

    get<VoteArticlePaths.CitizenVotesByIdsRequest> {
        val votes = repo.findCitizenVotesByTargets(it.citizen, it.id)
        if (votes.isNotEmpty()) {
            voter.assert { canView(votes, citizenOrNull) }
        }
        call.respond(votes)
    }
}
