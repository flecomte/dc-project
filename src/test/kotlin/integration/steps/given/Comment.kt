package integration.steps.given

import com.thedeanda.lorem.LoremIpsum
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.comment.generic.database.CommentForUpdate
import fr.dcproject.component.comment.generic.database.CommentForView
import fr.dcproject.component.comment.generic.database.CommentI
import fr.dcproject.component.comment.generic.database.CommentRef
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

fun <A : ArticleRef> createComment(
    id: UUID? = null,
    article: A? = null,
    createdBy: Name? = null,
    content: String? = null
): CommentForView<TargetRef, CitizenCreator> {
    val articleRepository: ArticleRepository by lazy { GlobalContext.get().koin.get() }
    return createCommentOnTarget(
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

fun <C : ConstitutionRef> createComment(
    id: UUID? = null,
    constitution: C? = null,
    createdBy: Name? = null,
    content: String? = null
): CommentForView<TargetRef, CitizenCreator> {
    val constitutionRepository: ConstitutionRepository by lazy { GlobalContext.get().koin.get() }
    return createCommentOnTarget(
        id,
        constitution?.id?.let { constitutionRepository.findById(constitution.id) } ?: createConstitution(constitution?.id),
        createdBy,
        content
    )
}

fun <T : TargetI> createCommentOnTarget(
    id: UUID? = null,
    target: T,
    createdBy: Name? = null,
    content: String? = null
): CommentForView<TargetRef, CitizenCreator> {
    val commentRepository: CommentRepository by lazy { GlobalContext.get().koin.get() }
    val creator = createCitizen(createdBy)
    val comment = CommentForUpdate(
        id = id ?: UUID.randomUUID(),
        createdBy = creator,
        target = target,
        content = content ?: LoremIpsum().getParagraphs(1, 3)
    )
    return commentRepository.comment(comment)
}

fun TestApplicationEngine.`Given I have comment on comment`(
    id: String? = null,
    parent: String? = null,
    createdBy: Name? = null,
    content: String? = null,
): CommentForView<out TargetRef, CitizenCreator> {
    return createCommentOnComment(
        id?.toUUID() ?: UUID.randomUUID(),
        parent?.run { CommentRef(toUUID()) },
        createdBy,
        content,
    )
}

fun createCommentOnComment(
    id: UUID? = null,
    parent: CommentI? = createComment<ArticleRef>(),
    createdBy: Name? = null,
    content: String? = null
): CommentForView<out TargetRef, CitizenCreator> {
    val creator = createCitizen(createdBy)
    val commentRepository: CommentRepository by lazy { GlobalContext.get().koin.get() }
    val parentComment = if (parent == null) {
        createComment<ArticleRef>()
    } else {
        commentRepository.findById(parent.id) ?: error("Parent of comment not found")
    }
    val comment = CommentForUpdate(
        id = id ?: UUID.randomUUID(),
        createdBy = creator,
        content = content ?: LoremIpsum().getParagraphs(1, 3),
        parent = parentComment,
    )
    return commentRepository.comment(comment)
}
