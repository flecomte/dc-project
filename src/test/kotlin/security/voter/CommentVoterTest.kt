package fr.dcproject.security.voter

import fr.dcproject.entity.*
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.can
import io.ktor.application.ApplicationCall
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@KtorExperimentalLocationsAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("voter")
internal class CommentVoterTest {
    private val tesla = CitizenBasic(
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla")
    )
    private val einstein = CitizenBasic(
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein")
    )

    private val article1 = Article(
        content = "Hi",
        createdBy = einstein,
        description = "blablabla",
        title = "Super article"
    )

    private val comment1 = Comment(
        content = "Hello",
        createdBy = tesla,
        target = article1
    )

    private val commentTargetDeleted = Comment(
        content = "Hello",
        createdBy = tesla,
        target = Article(
            content = "Hi",
            createdBy = einstein,
            description = "blablabla",
            title = "Super article"
        ).apply { deletedAt = DateTime.now() }
    )

    private val commentTargetNoUser = Comment(
        content = "Hello",
        createdBy = tesla,
        target = ArticleRef()
    )

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `support comment`() = CommentVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            supports(CommentVoter.Action.VIEW, it, comment1) `should be` true
            supports(CommentVoter.Action.VIEW, it, article1) `should be` false
            supports(p, it, comment1) `should be` false
        }
    }

    @Test
    fun `can be view the comment`() = listOf(CommentVoter(), ArticleVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CommentVoter.Action.VIEW, it, comment1) `should be` true
        }
    }

    @Test
    fun `can be view the comment list`() = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(CommentVoter.Action.VIEW, it, listOf(comment1)) `should be` true
        }
    }

    @Test
    fun `can be update your comment`() = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CommentVoter.Action.UPDATE, it, comment1) `should be` true
        }
    }

    @Test
    fun `can not be update other comment`() = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(CommentVoter.Action.UPDATE, it, comment1) `should be` false
        }
    }

    @Test
    fun `can not be delete your comment`() = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CommentVoter.Action.DELETE, it, comment1) `should be` false
        }
    }

    @Test
    fun `can be create a comment`() = listOf(CommentVoter(), ArticleVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CommentVoter.Action.CREATE, it, comment1) `should be` true
        }
    }

    @Test
    fun `can not be create a comment if target is deleted`() = listOf(CommentVoter(), ArticleVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CommentVoter.Action.CREATE, it, commentTargetDeleted) `should be` false
        }
    }

    @Test
    fun `can not be create a comment if target has no user`() = listOf(CommentVoter(), ArticleVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CommentVoter.Action.CREATE, it, commentTargetNoUser) `should be` false
        }
    }

    @Test
    fun `can not be create a comment with other creator`() = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(CommentVoter.Action.CREATE, it, comment1) `should be` false
        }
    }

    @Test
    fun `can not be create a comment if is null`() = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(CommentVoter.Action.CREATE, it, null) `should be` false
        }
    }

    @Test
    fun `can not be create a comment if not connected`() = listOf(CommentVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(CommentVoter.Action.CREATE, it, comment1) `should be` false
        }
    }
}