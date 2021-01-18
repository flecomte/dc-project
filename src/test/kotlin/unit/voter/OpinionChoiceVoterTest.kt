package unit.voter

import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.auth.user
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.OpinionChoice
import fr.dcproject.security.voter.OpinionChoiceVoter
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
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tag("voter")
internal class OpinionChoiceVoterTest {
    private val tesla = CitizenBasic(
        id = UUID.fromString("e6efc288-4283-4729-a268-6debb18de1a0"),
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "tesla@best.com",
        name = CitizenI.Name("Nicolas", "Tesla"),
        followAnonymous = false
    )

    private val tesla2 = CitizenCart(
        id = UUID.fromString("e6efc288-4283-4729-a268-6debb18de1a0"),
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Nicolas", "Tesla")
    )

    private val article1 = ArticleForView(
        content = "Hi",
        createdBy = tesla2,
        description = "blablabla",
        title = "Super article"
    )

    private val choice1 = OpinionChoice(
        name = "Opinion1",
        target = listOf()
    )

    init {
        mockkStatic("fr.dcproject.component.auth.CitizenContextKt")
    }

    @Test
    fun `support opinion choice`(): Unit = OpinionChoiceVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            this(OpinionChoiceVoter.Action.VIEW, it, choice1).vote `should be` Vote.GRANTED
            this(OpinionChoiceVoter.Action.VIEW, it, article1).vote `should be` Vote.ABSTAIN
            this(p, it, choice1).vote `should be` Vote.ABSTAIN
        }
    }

    @Test
    fun `can be view the opinion choice`(): Unit = listOf(OpinionChoiceVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionChoiceVoter.Action.VIEW, it, choice1) `should be` true
        }
    }

    @Test
    fun `can be view the opinion choice list`(): Unit = listOf(OpinionChoiceVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            canAll(OpinionChoiceVoter.Action.VIEW, it, listOf(choice1)) `should be` true
        }
    }
}