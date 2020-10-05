package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.ArticleRef
import fr.dcproject.entity.Citizen
import fr.dcproject.security.voter.FollowVoter.Action.CREATE
import fr.dcproject.security.voter.FollowVoter.Action.DELETE
import fr.dcproject.security.voter.FollowVoter.Action.VIEW
import fr.ktorVoter.assertCan
import fr.ktorVoter.assertCanAll
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import fr.dcproject.entity.Follow as FollowEntity
import fr.dcproject.repository.FollowArticle as FollowArticleRepository

@KtorExperimentalLocationsAPI
object FollowArticlePaths {
    @Location("/articles/{article}/follows")
    class ArticleFollowRequest(val article: ArticleRef)

    @Location("/citizens/{citizen}/follows/articles")
    class CitizenFollowArticleRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.followArticle(repo: FollowArticleRepository) {
    post<FollowArticlePaths.ArticleFollowRequest> {
        val follow = FollowEntity(target = it.article, createdBy = this.citizen)
        assertCan(CREATE, follow)
        repo.follow(follow)
        call.respond(HttpStatusCode.Created)
    }

    delete<FollowArticlePaths.ArticleFollowRequest> {
        val follow = FollowEntity(target = it.article, createdBy = this.citizen)
        assertCan(DELETE, follow)
        repo.unfollow(follow)
        call.respond(HttpStatusCode.NoContent)
    }

    get<FollowArticlePaths.ArticleFollowRequest> {
        repo.findFollow(citizen, it.article)?.let { follow ->
            assertCan(VIEW, follow)
            call.respond(follow)
        } ?: call.respond(HttpStatusCode.NoContent)
    }

    get<FollowArticlePaths.CitizenFollowArticleRequest> {
        val follows = repo.findByCitizen(it.citizen)
        if (follows.result.isNotEmpty()) {
            assertCanAll(VIEW, follows.result)
        }
        call.respond(follows)
    }
}