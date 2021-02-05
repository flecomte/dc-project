package fr.dcproject.component.article.routes

import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.ArticleForUpdate
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.article.routes.UpsertArticle.UpsertArticleRequest.Input
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.notification.ArticleUpdateNotification
import fr.dcproject.component.notification.Publisher
import fr.dcproject.component.workgroup.WorkgroupRef
import fr.dcproject.security.assert
import fr.dcproject.utils.receiveOrBadRequest
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object UpsertArticle {
    @Location("/articles")
    class UpsertArticleRequest {
        class Input(
            val id: UUID?,
            val title: String,
            val anonymous: Boolean = true,
            val content: String,
            val description: String,
            val tags: List<String> = emptyList(),
            val draft: Boolean = false,
            val versionId: UUID,
            val workgroup: WorkgroupRef? = null,
        )
    }

    fun Route.upsertArticle(repo: ArticleRepository, publisher: Publisher, ac: ArticleAccessControl) {
        suspend fun ApplicationCall.convertRequestToEntity(): ArticleForUpdate = receiveOrBadRequest<Input>().run {
            ArticleForUpdate(
                id = id ?: UUID.randomUUID(),
                title = title,
                anonymous = anonymous,
                content = content,
                description = description,
                tags = tags,
                draft = draft,
                createdBy = citizen,
                workgroup = workgroup,
                versionId = versionId
            )
        }

        post<UpsertArticleRequest> {
            val article = call.convertRequestToEntity()
            ac.assert { canUpsert(article, citizenOrNull) }
            val newArticle: ArticleForView = repo.upsert(article) ?: error("Article not updated")
            call.respond(newArticle)
            publisher.publish(ArticleUpdateNotification(newArticle))
        }
    }
}
