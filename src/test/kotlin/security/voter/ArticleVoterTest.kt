package fr.dcproject.security.voter

import fr.dcproject.entity.*
import fr.dcproject.user
import fr.ktorVoter.Vote
import fr.ktorVoter.can
import io.ktor.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("voter")
internal class ArticleVoterTest {
    val tesla = CitizenBasic(
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla")
    )
    val einstein = CitizenBasic(
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein")
    )

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `creator can be view the article`() = ArticleVoter().run {
        val article = getArticle(tesla).apply { draft = true }

        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            supports(ArticleVoter.Action.VIEW, it, article) `should be` true
            vote(ArticleVoter.Action.VIEW, it, article) `should be` Vote.GRANTED
        }
    }

    @Test
    fun `other user can be view the article`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(ArticleVoter.Action.VIEW, it, article) `should be` true
        }
    }

    @Test
    fun `other user can be view the article list`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla)
        val article2 = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(ArticleVoter.Action.VIEW, it, listOf(article, article2)) `should be` true
        }
    }

    @Test
    fun `the no creator can not be view the article on draft`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla).apply { draft = true }

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(ArticleVoter.Action.VIEW, it, article) `should be` false
        }
    }

    @Test
    fun `the no creator can not be view list of articles if one is on draft`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla)
        val article2 = getArticle(tesla).apply { draft = true }

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(ArticleVoter.Action.VIEW, it, listOf(article, article2)) `should be` false
        }
    }

    @Test
    fun `can not view deleted article`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla).apply { deletedAt = DateTime.now() }

        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(ArticleVoter.Action.VIEW, it, article) `should be` false
        }
    }

    @Test
    fun `can delete article if owner`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(ArticleVoter.Action.DELETE, it, article) `should be` true
        }
    }

    @Test
    fun `can not delete article if not owner`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla).apply { deletedAt = DateTime.now() }

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(ArticleVoter.Action.DELETE, it, article) `should be` false
        }
    }

    @Test
    fun `can create article if logged`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(ArticleVoter.Action.CREATE, it, article) `should be` true
        }
    }

    @Test
    fun `can not create article if not logged`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(ArticleVoter.Action.CREATE, it, article) `should be` false
        }
    }

    @Test
    fun `can update article if yours`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(ArticleVoter.Action.UPDATE, it, article) `should be` true
        }
    }

    @Test
    fun `can not update article if not yours`() = listOf(ArticleVoter()).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(ArticleVoter.Action.UPDATE, it, article) `should be` false
        }
    }

    private fun getArticle(createdBy: CitizenBasic = tesla) = Article(
        title = "Hello world",
        content = "Super",
        description = "I Rocks",
        createdBy = createdBy
    )
}