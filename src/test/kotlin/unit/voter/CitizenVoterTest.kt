package unit.voter

import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.voter.Vote.DENIED
import fr.dcproject.voter.Vote.GRANTED
import io.mockk.mockkStatic
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
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
        name = CitizenI.Name("Marie", "Curie"),
        deletedAt = DateTime.now()
    )

    init {
        mockkStatic("fr.dcproject.ApplicationContextKt")
    }

    @Test
    fun `can be view the citizen`() {
        CitizenVoter()
            .canView(subject = einstein, connectedCitizen = tesla)
            .vote `should be` GRANTED
    }

    @Test
    fun `can be view the citizen list`() {
        CitizenVoter()
            .canView(subjects = listOf(tesla, einstein), connectedCitizen = einstein)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not view deleted citizen`() {
        CitizenVoter()
            .canView(subject = curie, connectedCitizen = tesla)
            .vote `should be` DENIED
    }

    @Test
    fun `can be update itself`() {
        CitizenVoter()
            .canUpdate(subject = einstein, connectedCitizen = einstein)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be update other citizen`() {
        CitizenVoter()
            .canUpdate(subject = tesla, connectedCitizen = einstein)
            .vote `should be` DENIED
    }

    @Test
    fun `can be change password of itself`() {
        CitizenVoter()
            .canChangePassword(subject = einstein, connectedCitizen = einstein)
            .vote `should be` GRANTED
    }

    @Test
    fun `can not be change password of other citizen`() {
        CitizenVoter()
            .canChangePassword(subject = tesla, connectedCitizen = einstein)
            .vote `should be` DENIED
    }
}