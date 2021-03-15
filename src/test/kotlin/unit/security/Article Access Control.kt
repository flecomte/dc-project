package unit.security

import fr.dcproject.common.security.AccessDecision.DENIED
import fr.dcproject.common.security.AccessDecision.GRANTED
import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.database.ArticleForListing
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.auth.database.User
import fr.dcproject.component.auth.database.UserCreator
import fr.dcproject.component.auth.database.UserI
import fr.dcproject.component.citizen.database.CitizenCart
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import fr.postgresjson.connexion.Paginated
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.UUID
import fr.dcproject.component.article.database.ArticleRepository as ArticleRepo

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tags(Tag("security"), Tag("unit"))
internal class `Article Access Control` {
    private val tesla = CitizenCreator(
        id = UUID.fromString("e6efc288-4283-4729-a268-6debb18de1a0"),
        user = UserCreator(
            username = "nicolas-tesla",
        ),
        name = CitizenI.Name("Nicolas", "Tesla"),
        email = "nikola-tesla@volt.com"
    )
    private val einstein = CitizenCart(
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Albert", "Einstein")
    )

    private fun getRepo(article: ArticleForListing): ArticleRepo {
        return mockk {
            every { find(1, 1, any()) } returns Paginated(listOf(article), 0, 1, 1)
            every { findVersionsByVersionId(1, 1, any()) } returns Paginated(listOf(article), 0, 1, 1)
        }
    }

    @Test
    fun `creator can be view the article`() {
        val article = getArticle(tesla).copy(draft = true)
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canView(article, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `other user can be view the article`() {
        val article = getArticle(tesla)
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canView(article, einstein)
            .decision `should be` GRANTED
    }

    @Test
    fun `other user can be view the article list`(): Unit = listOf(ArticleAccessControl(mockk())).run {
        val article = getArticle(tesla)
        val article2 = getArticle(tesla)

        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canView(listOf(article, article2), einstein)
            .decision `should be` GRANTED
    }

    @Test
    fun `the no creator can not be view the article on draft`() {
        val article = getArticle(tesla).copy(draft = true)
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canView(article, einstein)
            .decision `should be` DENIED
    }

    @Test
    fun `the no creator can not be view list of articles if one is on draft`() {
        val article = getArticle(tesla)
        val article2 = getArticle(tesla).copy(draft = true)

        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canView(listOf(article, article2), einstein)
            .decision `should be` DENIED
    }

    @Test
    fun `can not view deleted article`() {
        val article = getArticle(tesla).copy(deletedAt = DateTime.now())
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canView(article, tesla)
            .decision `should be` DENIED
    }

    @Test
    fun `can delete article if owner`() {
        val article = getArticle(tesla)
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canDelete(article, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not delete article if not owner`() {
        val article = getArticle(tesla).copy(deletedAt = DateTime.now())
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canDelete(article, einstein)
            .code `should be` "article.delete.notYours"
    }

    @Test
    fun `can create article if logged`() {
        val article = getArticle(tesla)
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canUpsert(article, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not create article if not logged`() {
        val article = getArticle(tesla)
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canUpsert(article, null)
            .code `should be` "article.create.notConnected"
    }

    @Test
    fun `can update article if yours`() {
        val article = getArticle(tesla)
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canUpsert(article, tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not update article if not yours`() {
        val article = getArticle(tesla)
        ArticleAccessControl(getRepo(getArticleForListing(tesla)))
            .canUpsert(article, einstein)
            .code `should be` "article.update.notYours"
    }

    private fun getArticle(createdBy: CitizenCreator = tesla) = ArticleForView(
        id = UUID.randomUUID(),
        title = "Hello world",
        content = "Super",
        description = "I Rocks",
        createdBy = createdBy,
        opinions = mapOf(),
        versionId = UUID.randomUUID(),
        versionNumber = 1
    )

    private fun getArticleForListing(createdBy: CitizenCreator = tesla) = ArticleForListing(
        id = UUID.randomUUID(),
        title = "Hello world",
        createdBy = createdBy,
    )
}
