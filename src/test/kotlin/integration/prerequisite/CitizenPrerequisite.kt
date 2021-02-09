package integration.prerequisite

import fr.dcproject.component.auth.UserForCreate
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenForCreate
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRepository
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.get
import java.util.UUID

class CitizenPrerequisite : KoinComponent {
    fun createCitizen(
        firstName: String,
        lastName: String,
        email: String = ("$firstName-$lastName".toLowerCase()) + "@dc-project.fr",
        id: UUID = UUID.randomUUID()
    ): Citizen? {

        val user = UserForCreate(
            id = id,
            username = "$firstName-$lastName".toLowerCase(),
            password = "azerty",
        )
        val citizen = CitizenForCreate(
            id = id,
            name = CitizenI.Name(firstName, lastName),
            email = email,
            birthday = DateTime.now(),
            user = user
        )

        return get<CitizenRepository>().insertWithUser(citizen)
    }
}
