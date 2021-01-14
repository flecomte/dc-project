package fr.dcproject.security.voter

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleVoter
import fr.dcproject.entity.CitizenCart
import fr.dcproject.entity.CitizenI
import fr.dcproject.entity.User
import fr.dcproject.entity.UserI
import fr.dcproject.voter.Vote.DENIED
import fr.dcproject.voter.Vote.GRANTED
import fr.postgresjson.connexion.Paginated
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import fr.dcproject.component.article.ArticleRepository as ArticleRepo

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("voter")
class ArticleVoterTest {
    private val tesla = CitizenCart(
        id = UUID.fromString("e6efc288-4283-4729-a268-6debb18de1a0"),
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Nicolas", "Tesla")
    )
    private val einstein = CitizenCart(
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Albert", "Einstein")
    )

    private fun getRepo(article: ArticleForView): ArticleRepo {
        return mockk {
            every { findVersionsByVersionId(1, 1, any()) } returns Paginated(listOf(article), 0, 1, 1)
        }
    }

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `creator can be view the article`() {
        val article = getArticle(tesla).copy(draft = true)
        ArticleVoter(getRepo(article))
            .canView(article, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `other user can be view the article`() {
        val article = getArticle(tesla)
        ArticleVoter(getRepo(article))
            .canView(article, einstein)
            .vote `should be` GRANTED
    }

    @Test
    fun `other user can be view the article list`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla)
        val article2 = getArticle(tesla)

        ArticleVoter(getRepo(article))
            .canView(listOf(article, article2), einstein)
            .vote `should be` GRANTED
    }

    @Test
    fun `the no creator can not be view the article on draft`() {
        val article = getArticle(tesla).copy(draft = true)
        ArticleVoter(getRepo(article))
            .canView(article, einstein)
            .vote `should be` DENIED
    }

    @Test
    fun `the no creator can not be view list of articles if one is on draft`() {
        val article = getArticle(tesla)
        val article2 = getArticle(tesla).copy(draft = true)

        ArticleVoter(getRepo(article))
            .canView(listOf(article, article2), einstein)
            .vote `should be` DENIED
    }

    @Test
    fun `can not view deleted article`() {
        val article = getArticle(tesla).copy(deletedAt = DateTime.now())
        ArticleVoter(getRepo(article))
            .canView(article, tesla)
            .vote `should be` DENIED
    }

    @Test
    fun `can delete article if owner`() {
        val article = getArticle(tesla)
        ArticleVoter(getRepo(article))
            .canDelete(article, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not delete article if not owner`() {
        val article = getArticle(tesla).copy(deletedAt = DateTime.now())
        ArticleVoter(getRepo(article))
            .canDelete(article, einstein)
            .code `should be` "article.delete.notYours"
    }

    @Test
    fun `can create article if logged`() {
        val article = getArticle(tesla)
        ArticleVoter(getRepo(article))
            .canUpsert(article, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not create article if not logged`() {
        val article = getArticle(tesla)
        ArticleVoter(getRepo(article))
            .canUpsert(article, null)
            .code `should be` "article.create.notConnected"
    }

    @Test
    fun `can update article if yours`(): Unit {
        val article = getArticle(tesla)
        ArticleVoter(getRepo(article))
            .canUpsert(article, tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not update article if not yours`(): Unit {
        val article = getArticle(tesla)
        ArticleVoter(getRepo(article))
            .canUpsert(article, einstein)
            .code `should be` "article.update.notYours"
    }

    private fun getArticle(createdBy: CitizenCart = tesla) = ArticleForView(
        id = UUID.randomUUID(),
        title = "Hello world",
        content = "Super",
        description = "I Rocks",
        createdBy = createdBy,
        opinions = mapOf(),
        versionId = UUID.randomUUID(),
        versionNumber = 1
    )
}