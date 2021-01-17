package unit.voter

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.auth.user
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.Opinion
import fr.dcproject.entity.OpinionChoice
import fr.dcproject.security.voter.OpinionVoter
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tag("voter")
internal class OpinionVoterTest {
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

    private val opinion1 = Opinion(
        createdBy = tesla,
        target = article1,
        choice = OpinionChoice(
            name = "Opinion1",
            target = listOf("article")
        )
    )

    init {
        mockkStatic("fr.dcproject.component.auth.CitizenContextKt")
    }

    @Test
    fun `support opinion`(): Unit = OpinionVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            this(OpinionVoter.Action.VIEW, it, opinion1).vote `should be` Vote.GRANTED
            this(OpinionVoter.Action.VIEW, it, article1).vote `should be` Vote.GRANTED
            this(OpinionVoter.Action.VIEW, it, einstein).vote `should be` Vote.ABSTAIN
            this(p, it, opinion1).vote `should be` Vote.ABSTAIN
        }
    }

    @Test
    fun `can be view the opinion`(): Unit = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionVoter.Action.VIEW, it, opinion1) `should be` true
        }
    }

    @Test
    fun `can be not view the opinion if is null`(): Unit = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            assertThrows<NoSubjectDefinedException> {
                assertCan(OpinionVoter.Action.VIEW, it, null)
            }
        }
    }

    @Test
    fun `can be view the opinion list`(): Unit = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            canAll(OpinionVoter.Action.VIEW, it, listOf(opinion1)) `should be` true
        }
    }

    @Test
    fun `can be opinion an article`(): Unit = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionVoter.Action.CREATE, it, opinion1) `should be` true
        }
    }

    @Test
    fun `can not be opinion if not connected`() = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(OpinionVoter.Action.CREATE, it, opinion1) `should be` false
        }
    }

    @Test
    fun `can be remove opinion`(): Unit = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionVoter.Action.DELETE, it, opinion1) `should be` true
        }
    }

    @Test
    fun `can not be remove opinion if not connected`(): Unit = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(OpinionVoter.Action.DELETE, it, opinion1) `should be` false
        }
    }

    @Test
    fun `can not be remove opinion of other user`(): Unit = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(OpinionVoter.Action.DELETE, it, opinion1) `should be` false
        }
    }
}