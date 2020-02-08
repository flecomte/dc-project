package feature

import fr.dcproject.entity.*
import fr.dcproject.entity.ConstitutionSimple.TitleSimple
import fr.dcproject.entity.request.Constitution
import io.cucumber.java8.En
import org.joda.time.DateTime
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import java.util.concurrent.CompletionException
import fr.dcproject.entity.User as UserEntity
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.Constitution as ConstitutionRepository

class ConstitutionSteps : En, KoinTest {
    init {
        Given("I have constitution with id {string}") { id: String ->
            var citizen = Citizen(
                id = UUID.fromString(id),
                name = CitizenI.Name("John", "Doe"),
                email = "john.doe@gmail.com",
                birthday = DateTime.now(),
                user = UserEntity(username = "john-doe", plainPassword = "azerty")
            )

            try {
                get<CitizenRepository>().insertWithUser(citizen)
            } catch (e: CompletionException) {
                citizen = get<CitizenRepository>().findByUsername("john-doe")!!
            }

            val title1 = Constitution.Title(
                name = "My Title"
            )

            val constitution = Constitution(
                title = "hello",
                titles = mutableListOf(title1),
                anonymous = false
            )
            get<ConstitutionRepository>().upsert(constitution.create(citizen))
        }

        Given("I have constitution with id {string} created by {string}") { id: String, username: String ->
            val citizen = get<CitizenRepository>().findByUsername(username)!!

            val title1 = TitleSimple<ArticleRef>(
                name = "My Title"
            )

            val constitution = ConstitutionSimple<CitizenSimple, TitleSimple<ArticleRef>>(
                id = UUID.fromString(id),
                title = "hello",
                titles = mutableListOf(title1),
                anonymous = false,
                createdBy = citizen
            )
            get<ConstitutionRepository>().upsert(constitution)
        }
    }
}