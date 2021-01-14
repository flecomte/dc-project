package feature

import fr.dcproject.entity.Citizen
import fr.dcproject.entity.CitizenI
import fr.dcproject.entity.User
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import org.joda.time.DateTime
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import fr.dcproject.repository.Citizen as CitizenRepository

class CitizenSteps : En, KoinTest {
    init {
        Given("I have citizen") { extraData: DataTable? ->
            val params = extraData?.asMap<String, String>(String::class.java, String::class.java)
            createCitizen(
                params?.get("firstName") ?: "firstName" + UUID.randomUUID(),
                params?.get("lastName") ?: "lastName" + UUID.randomUUID(),
                extraData
            )
        }

        Given("I have citizen {word} {word}") { firstName: String, lastName: String ->
            createCitizen(firstName, lastName)
        }

        Given("I have citizen {word} {word} with") { firstName: String, lastName: String, extraData: DataTable? ->
            createCitizen(firstName, lastName, extraData)
        }

        Given("I have citizen {word} {word} with ID {string}") { firstName: String, lastName: String, id: String ->
            createCitizen(firstName, lastName, id = UUID.fromString(id))
        }
    }

    private fun createCitizen(firstName: String, lastName: String, extraData: DataTable? = null, id: UUID? = null) {
        val params = extraData?.asMap<String, String>(String::class.java, String::class.java)
        val id: UUID = id ?: params?.get("id")?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        val email = params?.get("email") ?: ("$firstName-$lastName".toLowerCase()) + "@dc-project.fr"

        val user = User(
            id = id,
            username = "$firstName-$lastName".toLowerCase(),
            plainPassword = "azerty"
        )
        val citizen = Citizen(
            id = id,
            name = CitizenI.Name(firstName, lastName),
            email = email,
            birthday = DateTime.now(),
            user = user
        )

        get<CitizenRepository>().insertWithUser(citizen)
    }
}