package feature

import fr.dcproject.entity.Citizen
import io.cucumber.java8.En
import org.joda.time.DateTime
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import java.util.concurrent.CompletionException
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.entity.Constitution.Title as TitleEntity
import fr.dcproject.entity.User as UserEntity
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.Constitution as ConstitutionRepository

class ConstitutionSteps: En, KoinTest {
    init {
        Given("I have constitution with id {string}") { id: String ->
            var citizen = Citizen(
                name = Citizen.Name("John", "Doe"),
                birthday = DateTime.now(),
                user = UserEntity(username = "john-doe", plainPassword = "azerty")
            )

            try {
                get<CitizenRepository>().insertWithUser(citizen)
            } catch (e: CompletionException) {
                citizen = get<CitizenRepository>().findByUsername("john-doe")!!
            }

            val title1 = TitleEntity(
                name = "My Title"
            )

            val constitution = ConstitutionEntity(
                id = UUID.fromString(id),
                title = "hello",
                titles = listOf(title1),
                createdBy = citizen,
                anonymous = false
            )
            get<ConstitutionRepository>().upsert(constitution)
        }

        Given("I have constitution with id {string} created by {string}") { id: String, username: String ->
            val citizen = get<CitizenRepository>().findByUsername(username)!!

            val title1 = TitleEntity(
                name = "My Title"
            )

            val constitution = ConstitutionEntity(
                id = UUID.fromString(id),
                title = "hello",
                titles = listOf(title1),
                createdBy = citizen,
                anonymous = false
            )
            get<ConstitutionRepository>().upsert(constitution)
        }
    }
}