package unit.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.entity.VoteForUpdate
import fr.dcproject.security.voter.VoteVoter
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.can
import fr.ktorVoter.canAll
import io.ktor.application.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.*
import fr.dcproject.entity.Vote as VoteEntity

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tag("voter")
internal class VoteVoterTest {
    private val tesla = Citizen(
        id = UUID.fromString("a1e35c99-9d33-4fb4-9201-58d7071243bb"),
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla"),
        followAnonymous = false
    )
    private val tesla3 = CitizenBasic(
        id = UUID.fromString("a1e35c99-9d33-4fb4-9201-58d7071243bb"),
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla"),
        followAnonymous = false
    )

    private val einstein = Citizen(
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

    private val article1 = ArticleForView(
        content = "Hi",
        createdBy = einstein2,
        description = "blablabla",
        title = "Super article"
    )

    private val vote1 = VoteEntity(
        createdBy = tesla3,
        target = article1,
        note = 1
    )

    private val voteForUpdate = VoteForUpdate(
        createdBy = tesla,
        target = article1,
        note = 1
    )

    private val voteOnDeleted = VoteForUpdate(
        createdBy = tesla,
        target = ArticleForView(
            content = "Hi",
            createdBy = einstein2,
            description = "blablabla",
            title = "Super article"
        ).copy(deletedAt = DateTime.now()),
        note = 1
    )

    private val voteWithoutTargetUser = VoteForUpdate(
        createdBy = tesla,
        target = ArticleRef(),
        note = 1
    )

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `support vote`(): Unit = VoteVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            this(VoteVoter.Action.VIEW, it, vote1).vote `should be` Vote.GRANTED
            this(VoteVoter.Action.VIEW, it, article1).vote `should be` Vote.ABSTAIN
            this(p, it, vote1).vote `should be` Vote.ABSTAIN
        }
    }

    @Test
    fun `can be view your the vote`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            can(VoteVoter.Action.VIEW, it, vote1) `should be` true
        }
    }

    @Test
    fun `can not be view vote of other`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns einstein
        }.let {
            can(VoteVoter.Action.VIEW, it, vote1) `should be` false
        }
    }

    @Test
    fun `can be not view the vote if is null`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            assertThrows<NoSubjectDefinedException> {
                can(VoteVoter.Action.VIEW, it, null)
            }
        }
    }

    @Test
    fun `can be view your votes list`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            canAll(VoteVoter.Action.VIEW, it, listOf(vote1)) `should be` true
        }
    }

    @Test
    fun `can be vote an article`() {
        listOf(VoteVoter()).run {
            mockk<ApplicationCall> {
                every { citizenOrNull } returns tesla
            }.let {
                can(VoteVoter.Action.CREATE, it, voteForUpdate) `should be` true
            }
        }
    }

    @Test
    fun `can not be vote if not connected`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns null
        }.let {
            can(VoteVoter.Action.CREATE, it, voteForUpdate) `should be` false
        }
    }

    @Test
    fun `can not be vote an article if article is deleted`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            can(VoteVoter.Action.CREATE, it, voteOnDeleted) `should be` false
        }
    }

    @Test
    fun `can not be vote an article if article have no user`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            assertThrows<NoSubjectDefinedException> {
                can(VoteVoter.Action.CREATE, it, voteWithoutTargetUser)
            }
        }
    }

    @Test
    fun `can not be comment an article if article is deleted`(): Unit = listOf(VoteVoter()).run {
        mockk<ApplicationCall> {
            every { citizenOrNull } returns tesla
        }.let {
            can(VoteVoter.Action.CREATE, it, voteOnDeleted) `should be` false
        }
    }
}