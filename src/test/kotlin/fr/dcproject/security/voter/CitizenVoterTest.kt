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
internal class CitizenVoterTest {
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

    private val curie = CitizenBasic(
        user = User(
            username = "marie-curie",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        birthday = DateTime.now(),
        email = "curie@best.com",
        name = CitizenI.Name("Marie", "Curie")
    ).apply { deletedAt = DateTime.now() }

    init {
        mockkStatic("fr.dcproject.security.voter.VoterKt")
    }

    @Test
    fun `support citizen`() = CitizenVoter().run {
        val p = object : ActionI {}
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            supports(CitizenVoter.Action.VIEW, it, einstein) `should be` true
            supports(p, it, einstein) `should be` false
        }
    }

    @Test
    fun `can be view the citizen`() = listOf(CitizenVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CitizenVoter.Action.VIEW, it, einstein) `should be` true
        }
    }

    @Test
    fun `can be view the citizen list`() = listOf(CitizenVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(CitizenVoter.Action.VIEW, it, listOf(einstein, tesla)) `should be` true
        }
    }

    @Test
    fun `can not view deleted citizen`() = listOf(CitizenVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns tesla.user
        }.let {
            can(CitizenVoter.Action.VIEW, it, curie) `should be` false
        }
    }

    @Test
    fun `can be update itself`() = listOf(CitizenVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(CitizenVoter.Action.UPDATE, it, einstein) `should be` true
        }
    }

    @Test
    fun `can not be update other citizen`() = listOf(CitizenVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(CitizenVoter.Action.UPDATE, it, tesla) `should be` false
        }
    }

    @Test
    fun `can be change password of itself`() = listOf(CitizenVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(CitizenVoter.Action.CHANGE_PASSWORD, it, einstein) `should be` true
        }
    }

    @Test
    fun `can not be change password of other citizen`() = listOf(CitizenVoter()).run {
        mockk<ApplicationCall> {
            every { user } returns einstein.user
        }.let {
            can(CitizenVoter.Action.CHANGE_PASSWORD, it, tesla) `should be` false
        }
    }
}