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
internal class OpinionChoiceVoterTest {
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

    private val article1 = Article(
        content = "Hi",
        createdBy = tesla,
        description = "blablabla",
        title = "Super article"
    )

    private val choice1 = OpinionChoice(
        name = "Opinion1",
        target = listOf()
    )

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `support opinion choice`() = OpinionChoiceVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            supports(OpinionChoiceVoter.Action.VIEW, it, choice1) `should be` true
            supports(OpinionChoiceVoter.Action.VIEW, it, article1) `should be` false
            supports(p, it, choice1) `should be` false
        }
    }

    @Test
    fun `can be view the opinion choice`() = listOf(OpinionChoiceVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionChoiceVoter.Action.VIEW, it, choice1) `should be` true
        }
    }

    @Test
    fun `can be view the opinion choice list`() = listOf(OpinionChoiceVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionChoiceVoter.Action.VIEW, it, listOf(choice1)) `should be` true
        }
    }
}