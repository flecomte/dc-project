package integration.steps.given

import com.thedeanda.lorem.LoremIpsum
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentRepository
import fr.dcproject.component.constitution.ConstitutionRef
import fr.dcproject.component.constitution.ConstitutionRepository
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have comment on article`(
    id: String? = null,
    article: String? = null,
    createdByUsername: String? = null,
    content: String? = null,
) {
    createComment(id?.toUUID(), ArticleRef(article?.toUUID()), createdByUsername, content)
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
    createdByUsername: String? = null,
    content: String? = null
) {
    val articleRepository: ArticleRepository by lazy { GlobalContext.get().koin.get() }
    createCommentOnTarget(
        id,
        article?.id?.let { articleRepository.findById(article.id) } ?: createArticle(article?.id),
        createdByUsername,
        content
    )
}

fun TestApplicationEngine.`Given I have comment on constitution`(
    id: String? = null,
    constitution: String? = null,
    createdByUsername: String? = null,
    content: String? = null,
) {
    createComment(id?.toUUID(), ConstitutionRef(constitution?.toUUID()), createdByUsername, content)
}

fun createComment(
    id: UUID? = null,
    constitution: ConstitutionRef? = null,
    createdByUsername: String? = null,
    content: String? = null
) {
    val constitutionRepository: ConstitutionRepository by lazy { GlobalContext.get().koin.get() }
    createCommentOnTarget(
        id,
        constitution?.id?.let { constitutionRepository.findById(constitution.id) } ?: createConstitution(constitution?.id),
        createdByUsername,
        content
    )
}

fun createCommentOnTarget(
    id: UUID? = null,
    target: TargetI,
    createdByUsername: String? = null,
    content: String? = null
) {
    val commentRepository: CommentRepository by lazy { GlobalContext.get().koin.get() }
    val createdBy = createCitizen(createdByUsername)
    val comment = CommentForUpdate(
        id = id ?: UUID.randomUUID(),
        createdBy = createdBy,
        target = target,
        content = content ?: LoremIpsum().getParagraphs(1, 3)
    )
    commentRepository.comment(comment)
}
