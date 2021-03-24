package integration.steps.given

import com.thedeanda.lorem.LoremIpsum
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.comment.generic.database.CommentForUpdate
import fr.dcproject.component.comment.generic.database.CommentRepository
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.dcproject.component.constitution.database.ConstitutionRepository
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have comment on article`(
    id: String? = null,
    article: String? = null,
    createdBy: Name? = null,
    content: String? = null,
) {
    createComment(id?.toUUID(), ArticleRef(article?.toUUID()), createdBy, content)
}

fun TestApplicationEngine.`Given I have comments on article`(
    numbers: Int,
    article: String? = null,
) {
    repeat(numbers) {
        createComment(article = ArticleRef(article?.toUUID()))
    }
}

fun createComment(
    id: UUID? = null,
    article: ArticleRef? = null,
    createdBy: Name? = null,
    content: String? = null
) {
    val articleRepository: ArticleRepository by lazy { GlobalContext.get().koin.get() }
    createCommentOnTarget(
        id,
        article?.id?.let { articleRepository.findById(article.id) } ?: createArticle(article?.id),
        createdBy,
        content
    )
}

fun TestApplicationEngine.`Given I have comment on constitution`(
    id: String? = null,
    constitution: String? = null,
    createdBy: Name? = null,
    content: String? = null,
) {
    createComment(id?.toUUID(), ConstitutionRef(constitution?.toUUID()), createdBy, content)
}

fun createComment(
    id: UUID? = null,
    constitution: ConstitutionRef? = null,
    createdBy: Name? = null,
    content: String? = null
) {
    val constitutionRepository: ConstitutionRepository by lazy { GlobalContext.get().koin.get() }
    createCommentOnTarget(
        id,
        constitution?.id?.let { constitutionRepository.findById(constitution.id) } ?: createConstitution(constitution?.id),
        createdBy,
        content
    )
}

fun createCommentOnTarget(
    id: UUID? = null,
    target: TargetI,
    createdBy: Name? = null,
    content: String? = null
) {
    val commentRepository: CommentRepository by lazy { GlobalContext.get().koin.get() }
    val creator = createCitizen(createdBy)
    val comment = CommentForUpdate(
        id = id ?: UUID.randomUUID(),
        createdBy = creator,
        target = target,
        content = content ?: LoremIpsum().getParagraphs(1, 3)
    )
    commentRepository.comment(comment)
}
