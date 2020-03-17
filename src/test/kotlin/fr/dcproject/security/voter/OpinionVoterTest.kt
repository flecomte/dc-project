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

    private val opinion1 = Opinion(
        createdBy = tesla,
        target = article1,
        choice = OpinionChoice(
            name = "Opinion1",
            target = listOf("article")
        )
    )

    init {
        mockkStatic("fr.dcproject.security.voter.VoterKt")
    }

    @Test
    fun `support opinion`() = OpinionVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            supports(OpinionVoter.Action.VIEW, it, opinion1) `should be` true
            supports(OpinionVoter.Action.VIEW, it, article1) `should be` false
            supports(p, it, opinion1) `should be` false
        }
    }

    @Test
    fun `can be view the opinion`() = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionVoter.Action.VIEW, it, opinion1) `should be` true
        }
    }

    @Test
    fun `can be not view the opinion if is null`() = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionVoter.Action.VIEW, it, null) `should be` false
        }
    }

    @Test
    fun `can be view the opinion list`() = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionVoter.Action.VIEW, it, listOf(opinion1)) `should be` true
        }
    }

    @Test
    fun `can be opinion an article`() = listOf(OpinionVoter()).run {
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
    fun `can be remove opinion`() = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(OpinionVoter.Action.DELETE, it, opinion1) `should be` true
        }
    }

    @Test
    fun `can not be remove opinion if not connected`() = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns null
        }.let {
            can(OpinionVoter.Action.DELETE, it, opinion1) `should be` false
        }
    }

    @Test
    fun `can not be remove opinion of other user`() = listOf(OpinionVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(OpinionVoter.Action.DELETE, it, opinion1) `should be` false
        }
    }
}