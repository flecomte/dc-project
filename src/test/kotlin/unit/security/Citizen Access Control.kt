package unit.security

import fr.dcproject.common.security.AccessDecision.DENIED
import fr.dcproject.common.security.AccessDecision.GRANTED
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.citizen.CitizenAccessControl
import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenI
import org.amshove.kluent.`should be`
import org.joda.time.DateTime
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(CONCURRENT)
@Tags(Tag("security"), Tag("unit"))
internal class `Citizen Access Control` {
    private val tesla = CitizenCart(
        user = User(
            username = "nicolas-tesla",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Nicolas", "Tesla")
    )
    private val einstein = CitizenCart(
        user = User(
            username = "albert-einstein",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Albert", "Einstein")
    )

    private val curie = CitizenCart(
        user = User(
            username = "marie-curie",
            roles = listOf(UserI.Roles.ROLE_USER)
        ),
        name = CitizenI.Name("Marie", "Curie"),
        deletedAt = DateTime.now()
    )

    @Test
    fun `can be view the citizen`() {
        CitizenAccessControl()
            .canView(subject = einstein, connectedCitizen = tesla)
            .decision `should be` GRANTED
    }

    @Test
    fun `can be view the citizen list`() {
        CitizenAccessControl()
            .canView(subjects = listOf(tesla, einstein), connectedCitizen = einstein)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not view deleted citizen`() {
        CitizenAccessControl()
            .canView(subject = curie, connectedCitizen = tesla)
            .decision `should be` DENIED
    }

    @Test
    fun `can be update itself`() {
        CitizenAccessControl()
            .canUpdate(subject = einstein, connectedCitizen = einstein)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be update other citizen`() {
        CitizenAccessControl()
            .canUpdate(subject = tesla, connectedCitizen = einstein)
            .decision `should be` DENIED
    }

    @Test
    fun `can be change password of itself`() {
        CitizenAccessControl()
            .canChangePassword(subject = einstein, connectedCitizen = einstein)
            .decision `should be` GRANTED
    }

    @Test
    fun `can not be change password of other citizen`() {
        CitizenAccessControl()
            .canChangePassword(subject = tesla, connectedCitizen = einstein)
            .decision `should be` DENIED
    }
}
