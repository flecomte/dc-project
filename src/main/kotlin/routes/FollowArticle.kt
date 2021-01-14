package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.entity.FollowForUpdate
import fr.dcproject.security.voter.FollowVoter.Action.*
import fr.ktorVoter.assertCan
import fr.ktorVoter.assertCanAll
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
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
        val follow = FollowForUpdate(target = it.article, createdBy = this.citizen)
        assertCan(CREATE, follow)
        repo.follow(follow)
        call.respond(HttpStatusCode.Created)
    }

    delete<FollowArticlePaths.ArticleFollowRequest> {
        val follow = FollowForUpdate(target = it.article, createdBy = this.citizen)
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