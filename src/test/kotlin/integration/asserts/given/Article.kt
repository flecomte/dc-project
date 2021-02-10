package integration.asserts.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.ArticleForUpdate
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.auth.UserForCreate
import fr.dcproject.component.citizen.CitizenForCreate
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.workgroup.WorkgroupRef
import io.ktor.server.testing.TestApplicationEngine
import org.joda.time.DateTime
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

private fun createArticle(
    id: UUID? = null,
    workgroup: WorkgroupRef? = null,
    createdByUsername: String? = null
) {
    val username = (createdByUsername ?: "username" + UUID.randomUUID().toString())
        .toLowerCase().replace(' ', '-')

    val citizenRepository: CitizenRepository by lazy<CitizenRepository> { GlobalContext.get().koin.get() }
    val articleRepository: ArticleRepository by lazy<ArticleRepository> { GlobalContext.get().koin.get() }

    val createdBy = if (createdByUsername != null) {
        citizenRepository.findByUsername(username) ?: error("Citizen not exist")
    } else {
        val first = "firstName" + UUID.randomUUID().toString()
        val last = "lastName" + UUID.randomUUID().toString()
        CitizenForCreate(
            birthday = DateTime.now(),
            name = CitizenI.Name(
                first,
                last
            ),
            email = "$first@fakeemail.com",
            user = UserForCreate(username = username, password = "azerty")
        ).let {
            citizenRepository.insertWithUser(it) ?: error("Unable to create User")
        }
    }

    val article = ArticleForUpdate(
        id = id ?: UUID.randomUUID(),
        title = "hello",
        content = "bla bla bla",
        description = "A super article",
        createdBy = createdBy,
        workgroup = workgroup,
        versionId = UUID.randomUUID()
    )
    articleRepository.upsert(article)
}
