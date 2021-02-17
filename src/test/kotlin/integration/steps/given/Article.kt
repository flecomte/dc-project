package integration.steps.given

import com.thedeanda.lorem.LoremIpsum
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.ArticleForUpdate
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.workgroup.WorkgroupRef
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have article`(
    id: String? = null,
    workgroup: WorkgroupRef? = null,
    createdByUsername: String? = null
) {
    createArticle(id?.toUUID(), workgroup, createdByUsername)
}

fun TestApplicationEngine.`Given I have articles`(
    numbers: Int,
) {
    repeat(numbers) {
        createArticle()
    }
}
fun TestApplicationEngine.`Given I have article created by workgroup`(
    workgroupId: String,
) {
    createArticle(workgroup = WorkgroupRef(workgroupId.toUUID()))
}

fun createArticle(
    id: UUID? = null,
    workgroup: WorkgroupRef? = null,
    createdByUsername: String? = null
): ArticleForView {
    val articleRepository: ArticleRepository by lazy { GlobalContext.get().koin.get() }

    val createdBy = createCitizen(createdByUsername)

    val article = ArticleForUpdate(
        id = id ?: UUID.randomUUID(),
        title = LoremIpsum().getTitle(3),
        content = LoremIpsum().getParagraphs(1, 2),
        description = LoremIpsum().getParagraphs(1, 2),
        createdBy = createdBy,
        workgroup = workgroup,
        versionId = UUID.randomUUID()
    )
    return articleRepository.upsert(article) ?: error("Cannot create article")
}
