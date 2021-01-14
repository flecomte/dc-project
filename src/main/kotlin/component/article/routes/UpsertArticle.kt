package fr.dcproject.component.article.route

import fr.dcproject.citizen
import fr.dcproject.citizenOrNull
import fr.dcproject.component.article.ArticleForUpdate
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.article.ArticleVoter
import fr.dcproject.entity.WorkgroupRef
import fr.dcproject.event.ArticleUpdate
import fr.dcproject.event.raiseEvent
import fr.dcproject.repository.Workgroup
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import java.util.*

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
fun Route.upsertArticle(repo: ArticleRepository, workgroupRepository: Workgroup, voter: ArticleVoter) {
    suspend fun PipelineContext<Unit, ApplicationCall>.convertDtoToEntity(): ArticleForUpdate = call.receive<PostArticleRequest.Input>().run {
        ArticleForUpdate(
            id = id ?: UUID.randomUUID(),
            title = title,
            anonymous = anonymous,
            content = content,
            description = description,
            tags = tags,
            draft = draft,
            createdBy = call.citizen,
            workgroup = if (workgroup != null) workgroupRepository.findById(workgroup.id) else null,
            versionId = versionId
        )
    }

    post<PostArticleRequest> {
        val article = convertDtoToEntity()

        voter.assert { canUpsert(article, citizenOrNull) }

        val newArticle: ArticleForView = repo.upsert(article) ?: error("Article not updated")

        call.respond(newArticle)

        raiseEvent(ArticleUpdate.event, ArticleUpdate(newArticle))
    }
}
