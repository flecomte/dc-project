package fr.dcproject.component.article.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.database.ArticleForUpdate
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.article.routes.UpsertArticle.UpsertArticleRequest.Input
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.notification.ArticleUpdateNotificationMessage
import fr.dcproject.component.notification.NotificationPublisherAsync
import fr.dcproject.component.workgroup.database.WorkgroupRef
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxItems
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minItems
import io.konform.validation.jsonschema.minLength
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
            val tags: Set<String> = emptySet(),
            val draft: Boolean = false,
            val versionId: UUID,
            val workgroup: WorkgroupRef? = null,
        ) {
            fun validate() = Validation<Input> {
                Input::title {
                    minLength(5)
                    maxLength(80)
                }
                Input::content {
                    minLength(50)
                    maxLength(6000)
                }
                Input::description {
                    minLength(50)
                    maxLength(6000)
                }
                Input::tags {
                    minItems(0)
                    maxItems(15)
                }
            }.validate(this)
        }
    }

    fun Route.upsertArticle(repo: ArticleRepository, notificationPublisher: NotificationPublisherAsync, ac: ArticleAccessControl) {
        suspend fun ApplicationCall.convertRequestToEntity(): ArticleForUpdate = receiveOrBadRequest<Input>().run {
            validate().badRequestIfNotValid()
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
            mustBeAuth()
            val article = call.convertRequestToEntity()
            ac.assert { canUpsert(article, citizenOrNull) }
            repo.upsert(article)?.let { a ->
                call.respond(
                    object {
                        val id: UUID = a.id
                        val versionId = a.versionId
                        val versionNumber = a.versionNumber
                    }
                )
                notificationPublisher.publishAsync(ArticleUpdateNotificationMessage(a))
            } ?: error("Article not updated")
        }
    }
}
