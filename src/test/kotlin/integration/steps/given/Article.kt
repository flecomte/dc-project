package integration.steps.given

import com.thedeanda.lorem.LoremIpsum
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleForUpdate
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.workgroup.database.WorkgroupRef
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have article`(
    id: String? = null,
    workgroup: WorkgroupRef? = null,
    createdBy: Name? = null
) {
    createArticle(id?.toUUID(), workgroup, createdBy)
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
    createdBy: Name? = null
): ArticleForView {
    val articleRepository: ArticleRepository by lazy { GlobalContext.get().koin.get() }

    val citizen = createCitizen(createdBy)

    val article = ArticleForUpdate(
        id = id ?: UUID.randomUUID(),
        title = LoremIpsum().getTitle(3),
        content = LoremIpsum().getParagraphs(1, 2),
        description = LoremIpsum().getParagraphs(1, 2),
        createdBy = citizen,
        workgroup = workgroup,
        versionId = UUID.randomUUID()
    )
    return articleRepository.upsert(article) ?: error("Cannot create article")
}
