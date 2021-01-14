package fr.dcproject.security.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.entity.*
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.can
import fr.ktorVoter.canAll
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
    private val tesla2 = Citizen(
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
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein"),
        followAnonymous = true
    )

    private val einstein2 = CitizenCart(
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Albert", "Einstein")
    )

    private val einstein3 = Citizen(
        id = UUID.fromString("319f1226-8f47-4df3-babd-2c7671ad0fbc"),
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "einstein@best.com",
        name = CitizenI.Name("Albert", "Einstein"),
        followAnonymous = true
    )

    private val article1 = ArticleForView(
        content = "Hi",
        createdBy = einstein2,
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
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `support follow`(): Unit = FollowVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla2
        }.let {
            this(FollowVoter.Action.VIEW, it, follow1).vote `should be` Vote.GRANTED
            assertThrows<NoSubjectDefinedException> {
                this(FollowVoter.Action.VIEW, it, article1)
            }
            this(p, it, follow1).vote `should be` Vote.ABSTAIN
        }
    }

    @Test
    fun `can be view the follow`(): Unit = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla2
        }.let {
            can(FollowVoter.Action.VIEW, it, follow1) `should be` true
        }
    }

    @Test
    fun `can be view the follow list`(): Unit = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla2
        }.let {
            canAll(FollowVoter.Action.VIEW, it, listOf(follow1)) `should be` true
        }
    }

    @Test
    fun `can be view your anonymous follow`(): Unit = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns einstein3
        }.let {
            can(FollowVoter.Action.VIEW, it, followAnon) `should be` true
        }
    }

    @Test
    fun `can not be view the anonymous follow of other`(): Unit = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla2
        }.let {
            can(FollowVoter.Action.VIEW, it, followAnon) `should be` false
        }
    }

    @Test
    fun `can be follow article`(): Unit = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla2
        }.let {
            can(FollowVoter.Action.CREATE, it, follow1) `should be` true
        }
    }

    @Test
    fun `can not be follow article if not connected`(): Unit = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns null
        }.let {
            can(FollowVoter.Action.CREATE, it, follow1) `should be` false
        }
    }

    @Test
    fun `can be unfollow article`(): Unit = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla2
        }.let {
            can(FollowVoter.Action.DELETE, it, follow1) `should be` true
        }
    }

    @Test
    fun `can not be unfollow article if not connected`(): Unit = listOf(FollowVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns null
        }.let {
            can(FollowVoter.Action.DELETE, it, follow1) `should be` false
        }
    }
}