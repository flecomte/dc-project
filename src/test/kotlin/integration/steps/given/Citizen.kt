package integration.steps.given

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
    id: String = UUID.randomUUID().toString(),
    callback: Citizen.() -> Unit = {}
): Citizen? {
    val repo: CitizenRepository by lazy { GlobalContext.get().koin.get() }

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

    return repo.insertWithUser(citizen)?.also { callback(it) }
}

fun createCitizen(createdByUsername: String? = null): Citizen {
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().koin.get() }

    val username = (createdByUsername ?: "username" + UUID.randomUUID().toString())
        .toLowerCase().replace(' ', '-')

    return if (createdByUsername != null) {
        citizenRepository.findByUsername(createdByUsername) ?: error("Citizen not exist")
    } else {
        val first = "firstName" + UUID.randomUUID().toString()
        val last = "lastName" + UUID.randomUUID().toString()
        CitizenForCreate(
            birthday = DateTime.now(),
            name = CitizenI.Name(
                first,
                last
            ),
            email = "$first@fakeemail.com",
            user = UserForCreate(username = username, password = "azerty")
        ).let {
            citizenRepository.insertWithUser(it) ?: error("Unable to create User")
        }
    }
}
