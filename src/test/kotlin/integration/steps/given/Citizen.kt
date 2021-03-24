package integration.steps.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.auth.database.UserForCreate
import fr.dcproject.component.citizen.database.Citizen
import fr.dcproject.component.citizen.database.CitizenForCreate
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRepository
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

fun createCitizen(createdBy: CitizenI.Name? = null): Citizen {
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().koin.get() }

    return if (createdBy != null) {
        citizenRepository.findByName(createdBy) ?: error("Citizen not exist")
    } else {
        val first = "firstName" + UUID.randomUUID().toString()
        val last = "lastName" + UUID.randomUUID().toString()
        val username = ("username" + UUID.randomUUID().toString())
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
