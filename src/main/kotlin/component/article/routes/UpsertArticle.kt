package fr.dcproject.component.article.routes

import fr.dcproject.component.article.ArticleForUpdate
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.article.ArticleVoter
import fr.dcproject.component.article.routes.PostArticleRequest.Input
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.workgroup.WorkgroupRef
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.event.ArticleUpdate
import fr.dcproject.event.raiseEvent
import fr.dcproject.voter.assert
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
@Location("/articles")
class PostArticleRequest {
    class Input(
        val id: UUID?,
        val title: String,
        val anonymous: Boolean = true,
        val content: String,
        val description: String,
        val tags: List<String> = emptyList(),
        val draft: Boolean = false,
        val versionId: UUID?,
        val workgroup: WorkgroupRef? = null
    )
}

@KtorExperimentalLocationsAPI
fun Route.upsertArticle(repo: ArticleRepository, workgroupRepository: WorkgroupRepository, voter: ArticleVoter) {
    suspend fun ApplicationCall.convertRequestToEntity(): ArticleForUpdate = receive<Input>().run {
        ArticleForUpdate(
            id = id ?: UUID.randomUUID(),
            title = title,
            anonymous = anonymous,
            content = content,
            description = description,
            tags = tags,
            draft = draft,
            createdBy = citizen,
            workgroup = if (workgroup != null) workgroupRepository.findById(workgroup.id) else null,
            versionId = versionId
        )
    }

    post<PostArticleRequest> {
        val article = call.convertRequestToEntity()

        voter.assert { canUpsert(article, citizenOrNull) }

        val newArticle: ArticleForView = repo.upsert(article) ?: error("Article not updated")

        call.respond(newArticle)

        raiseEvent(ArticleUpdate.event, ArticleUpdate(newArticle))
    }
}
