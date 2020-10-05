package fr.dcproject.security.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.entity.*
import fr.dcproject.user
import fr.ktorVoter.Vote
import fr.ktorVoter.can
import fr.ktorVoter.canAll
import fr.postgresjson.connexion.Paginated
import io.ktor.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import fr.dcproject.repository.Article as ArticleRepo

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("voter")
class ArticleVoterTest {
    private val tesla = Citizen(
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla")
    )
    private val einstein = Citizen(
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein")
    )

    private fun getRepo(article: Article): ArticleRepo {
        return mockk {
            every { findVerionsByVersionsId(1, 1, any()) } returns Paginated(listOf(article), 0, 1, 1)
        }
    }

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `creator can be view the article`(): Unit {
        val article = getArticle(tesla).apply { draft = true }
        ArticleVoter(getRepo(article)).run {
            mockk<ApplicationCall> {
                every { user } returns tesla.user
            }.let {
                this(ArticleVoter.Action.VIEW, it, article) `should be` Vote.GRANTED
            }
        }
    }

    @Test
    fun `other user can be view the article`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(ArticleVoter.Action.VIEW, it, article) `should be` true
        }
    }

    @Test
    fun `other user can be view the article list`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla)
        val article2 = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            canAll(ArticleVoter.Action.VIEW, it, listOf(article, article2)) `should be` true
        }
    }

    @Test
    fun `the no creator can not be view the article on draft`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla).apply { draft = true }

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(ArticleVoter.Action.VIEW, it, article) `should be` false
        }
    }

    @Test
    fun `the no creator can not be view list of articles if one is on draft`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla)
        val article2 = getArticle(tesla).apply { draft = true }

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            canAll(ArticleVoter.Action.VIEW, it, listOf(article, article2)) `should be` false
        }
    }

    @Test
    fun `can not view deleted article`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla).apply { deletedAt = DateTime.now() }

        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(ArticleVoter.Action.VIEW, it, article) `should be` false
        }
    }

    @Test
    fun `can delete article if owner`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(ArticleVoter.Action.DELETE, it, article) `should be` true
        }
    }

    @Test
    fun `can not delete article if not owner`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla).apply { deletedAt = DateTime.now() }

        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(ArticleVoter.Action.DELETE, it, article) `should be` false
        }
    }

    @Test
    fun `can create article if logged`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(ArticleVoter.Action.CREATE, it, article) `should be` true
        }
    }

    @Test
    fun `can not create article if not logged`(): Unit = listOf(ArticleVoter(mockk())).run {
        val article = getArticle(tesla)

        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(ArticleVoter.Action.CREATE, it, article) `should be` false
        }
    }

    @Test
    fun `can update article if yours`(): Unit {
        val article = getArticle(tesla)
        listOf(ArticleVoter(getRepo(article))).run {
            mockk<ApplicationCall> {
                every { user } returns tesla.user
                every { citizenOrNull } returns tesla
            }.let {
                can(ArticleVoter.Action.UPDATE, it, article) `should be` true
            }
        }
    }

    @Test
    fun `can not update article if not yours`(): Unit {
        val article = getArticle(tesla)
        listOf(ArticleVoter(getRepo(article))).run {
            mockk<ApplicationCall> {
                every { user } returns einstein.user
                every { citizenOrNull } returns einstein
            }.let {
                can(ArticleVoter.Action.UPDATE, it, article) `should be` false
            }
        }
    }

    private fun getArticle(createdBy: CitizenBasic = tesla) = Article(
        title = "Hello world",
        content = "Super",
        description = "I Rocks",
        createdBy = createdBy
    )
}