package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.Citizen
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Follow as FollowEntity
import fr.dcproject.repository.FollowArticle as FollowArticleRepository

@KtorExperimentalLocationsAPI
object FollowArticlePaths {
    @Location("/articles/{article}/follows") class ArticleFollowRequest(val article: ArticleEntity)
    @Location("/citizens/{citizen}/follows/articles") class CitizenFollowArticleRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.followArticle(repo: FollowArticleRepository) {
    post<FollowArticlePaths.ArticleFollowRequest> {
        val follow = FollowEntity(target = it.article, createdBy = this.citizen)
        // TODO create voter
//        assertCan(FollowVoter.Action.CREATE, follow)
        repo.follow(follow)
        call.respond(HttpStatusCode.Created)
    }

    delete<FollowArticlePaths.ArticleFollowRequest> {
        val follow = FollowEntity(target = it.article, createdBy = this.citizen)
        // TODO create voter
//        assertCan(FollowVoter.Action.DELETE, follow)
        repo.unfollow(follow)
        call.respond(HttpStatusCode.NoContent)
    }

    get<FollowArticlePaths.CitizenFollowArticleRequest> {
        val follows = repo.findByCitizen(it.citizen)
        // TODO add security
//        assertCan(FollowVoter.Action.VIEW, follows)
        call.respond(follows)
    }
}