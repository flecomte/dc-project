package fr.dcproject.security.voter

import fr.dcproject.entity.*
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
internal class FollowVoterTest {
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

    private val follow1 = Follow(
        createdBy = tesla,
        target = article1
    )

    private val followAnon = Follow(
        createdBy = einstein,
        target = article1
    )

    init {
        mockkStatic("fr.dcproject.security.voter.VoterKt")
    }

    @Test
    fun `support follow`() = FollowVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            supports(FollowVoter.Action.VIEW, it, follow1) `should be` true
            supports(FollowVoter.Action.VIEW, it, article1) `should be` false
            supports(p, it, follow1) `should be` false
        }
    }

    @Test
    fun `can be view the follow`() = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(FollowVoter.Action.VIEW, it, follow1) `should be` true
        }
    }

    @Test
    fun `can be view the follow list`() = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(FollowVoter.Action.VIEW, it, listOf(follow1)) `should be` true
        }
    }

    @Test
    fun `can be view your anonymous follow`() = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(FollowVoter.Action.VIEW, it, followAnon) `should be` true
        }
    }

    @Test
    fun `can not be view the anonymous follow of other`() = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(FollowVoter.Action.VIEW, it, followAnon) `should be` false
        }
    }

    @Test
    fun `can be follow article`() = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(FollowVoter.Action.CREATE, it, follow1) `should be` true
        }
    }

    @Test
    fun `can not be follow article if not connected`() = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(FollowVoter.Action.CREATE, it, follow1) `should be` false
        }
    }

    @Test
    fun `can be unfollow article`() = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(FollowVoter.Action.DELETE, it, follow1) `should be` true
        }
    }

    @Test
    fun `can not be unfollow article if not connected`() = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(FollowVoter.Action.DELETE, it, follow1) `should be` false
        }
    }
}