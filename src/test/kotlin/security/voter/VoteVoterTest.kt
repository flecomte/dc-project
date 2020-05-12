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
import fr.dcproject.entity.Vote as VoteEntity

@KtorExperimentalLocationsAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("voter")
internal class VoteVoterTest {
    private val tesla = CitizenBasic(
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla"),
        followAnonymous = false
    )

    private val einstein = CitizenBasic(
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein"),
        followAnonymous = true
    )

    private val article1 = Article(
        content = "Hi",
        createdBy = einstein,
        description = "blablabla",
        title = "Super article"
    )

    private val vote1 = VoteEntity(
        createdBy = tesla,
        target = article1,
        note = 1
    )

    private val voteOnDeleted = VoteEntity(
        createdBy = tesla,
        target = Article(
            content = "Hi",
            createdBy = einstein,
            description = "blablabla",
            title = "Super article"
        ).apply { deletedAt = DateTime.now() },
        note = 1
    )

    private val voteWithoutUser = VoteEntity(
        createdBy = tesla,
        target = ArticleRef(),
        note = 1
    )

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `support vote`() = VoteVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            supports(VoteVoter.Action.VIEW, it, vote1) `should be` true
            supports(VoteVoter.Action.VIEW, it, article1) `should be` false
            supports(p, it, vote1) `should be` false
        }
    }

    @Test
    fun `can be view your the vote`() = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.VIEW, it, vote1) `should be` true
        }
    }

    @Test
    fun `can not be view vote of other`() = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(VoteVoter.Action.VIEW, it, vote1) `should be` false
        }
    }

    @Test
    fun `can be not view the vote if is null`() = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.VIEW, it, null) `should be` false
        }
    }

    @Test
    fun `can be view your votes list`() = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.VIEW, it, listOf(vote1)) `should be` true
        }
    }

    @Test
    fun `can be vote an article`() = listOf(VoteVoter(), ArticleVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.CREATE, it, vote1) `should be` true
        }
    }

    @Test
    fun `can not be vote if not connected`() = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(VoteVoter.Action.CREATE, it, vote1) `should be` false
        }
    }

    @Test
    fun `can not be vote an article if article is deleted`() = listOf(VoteVoter(), ArticleVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.CREATE, it, voteOnDeleted) `should be` false
        }
    }

    @Test
    fun `can not be vote an article if article have no user`() = listOf(VoteVoter(), ArticleVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(VoteVoter.Action.CREATE, it, voteWithoutUser) `should be` false
        }
    }

    @Test
    fun `can not be comment an article if article is deleted`() = listOf(VoteVoter(), ArticleVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CommentVoter.Action.CREATE, it, voteOnDeleted) `should be` false
        }
    }
}