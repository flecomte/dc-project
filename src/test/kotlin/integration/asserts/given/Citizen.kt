package integration.asserts.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.auth.UserForCreate
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenForCreate
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRepository
import io.ktor.server.testing.TestApplicationEngine
import org.joda.time.DateTime
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have citizen`(
    firstName: String,
    lastName: String,
    email: String = ("$firstName-$lastName".toLowerCase()) + "@dc-project.fr",
    id: String = UUID.randomUUID().toString()
): Citizen? {
    val repo: CitizenRepository by lazy<CitizenRepository> { GlobalContext.get().koin.get() }

    val user = UserForCreate(
        id = id.toUUID(),
        username = "$firstName-$lastName".toLowerCase(),
        password = "azerty",
    )
    val citizen = CitizenForCreate(
        id = id.toUUID(),
        name = CitizenI.Name(firstName, lastName),
        email = email,
        birthday = DateTime.now(),
        user = user
    )

    return repo.insertWithUser(citizen)
}
