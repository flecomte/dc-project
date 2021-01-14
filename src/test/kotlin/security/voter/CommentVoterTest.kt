package fr.dcproject.security.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.CommentForUpdate
import fr.dcproject.entity.CommentForView
import fr.dcproject.entity.User
import fr.dcproject.entity.UserI
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
import fr.postgresjson.connexion.Paginated
import io.ktor.application.*
import io.ktor.locations.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.*
import fr.dcproject.component.article.ArticleRepository as ArticleRepo

@KtorExperimentalLocationsAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("voter")
internal class CommentVoterTest {
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
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein")
    )

    private val einstein2 = CitizenCart(
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Albert", "Einstein")
    )

    private val article1 = ArticleForView(
        content = "Hi",
        createdBy = einstein2,
        description = "blablabla",
        title = "Super article"
    )

    private val comment1 = CommentForView(
        content = "Hello",
        createdBy = tesla,
        target = article1
    )

    private val commentForUpdate = CommentForUpdate(
        content = "Hello",
        createdBy = tesla,
        target = article1
    )

    private val comment2 = CommentForView(
        content = "Hello2",
        createdBy = einstein,
        target = article1
    )

    private val commentTargetDeleted = CommentForView(
        content = "Hello",
        createdBy = tesla,
        target = ArticleForView(
            content = "Hi",
            createdBy = einstein2,
            description = "blablabla",
            title = "Super article",
            workgroup = null
        ).copy(deletedAt = DateTime.now())
    )

    private val commentTargetNoUser = CommentForView(
        content = "Hello",
        createdBy = tesla,
        target = ArticleRef()
    )

    private val repoArticle1 = mockk<ArticleRepo> {
        every { findVersionsByVersionId(1, 1, any()) } returns Paginated(listOf(article1), 0, 1, 1)
    }

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `support comment`(): Unit = CommentVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            this(CommentVoter.Action.VIEW, it, comment1).vote `should be` Vote.GRANTED
            this(CommentVoter.Action.VIEW, it, article1).vote `should be` Vote.ABSTAIN
            this(p, it, comment1).vote `should be` Vote.ABSTAIN
        }
    }

    @Test
    fun `can be view the comment`(): Unit {
        listOf(CommentVoter()).run {
            mockk<ApplicationCall> {
                every { citizenOrNull } returns tesla
            }.let {
                can(CommentVoter.Action.VIEW, it, comment1) `should be` true
            }
        }
    }

    @Test
    fun `can be view the comment list`(): Unit = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns einstein
        }.let {
            canAll(CommentVoter.Action.VIEW, it, listOf(comment1)) `should be` true
        }
    }

    @Test
    fun `can be update your comment`(): Unit = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            can(CommentVoter.Action.UPDATE, it, comment1) `should be` true
        }
    }

    @Test
    fun `can not be update other comment`(): Unit = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns einstein
        }.let {
            can(CommentVoter.Action.UPDATE, it, comment1) `should be` false
        }
    }

    @Test
    fun `can not be delete your comment`(): Unit = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            can(CommentVoter.Action.DELETE, it, comment1) `should be` false
        }
    }

    @Test
    fun `can be create a comment`(): Unit = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            can(CommentVoter.Action.CREATE, it, comment1) `should be` true
        }
    }

    @Test
    fun `can not be create a comment if target is deleted`(): Unit = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            can(CommentVoter.Action.CREATE, it, commentTargetDeleted) `should be` false
        }
    }

    @Test
    fun `can not be create a comment with other creator`(): Unit = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns einstein
        }.let {
            can(CommentVoter.Action.CREATE, it, comment1) `should be` false
        }
    }

    @Test
    fun `can not be create a comment if is null`(): Unit = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns einstein
        }.let {
            assertThrows<NoSubjectDefinedException> {
                assertCan(CommentVoter.Action.CREATE, it, null)
            }
        }
    }

    @Test
    fun `can not be create a comment if not connected`(): Unit = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns null
        }.let {
            can(CommentVoter.Action.CREATE, it, comment1) `should be` false
        }
    }
}