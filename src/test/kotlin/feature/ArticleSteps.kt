package feature

import fr.dcproject.entity.Citizen
import io.cucumber.java8.En
import org.joda.time.DateTime
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import java.util.concurrent.CompletionException
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.User as UserEntity
import fr.dcproject.repository.Article as ArticleRepository
import fr.dcproject.repository.Citizen as CitizenRepository

class ArticleSteps: En, KoinTest {
    init {
        Given("I have article with id {string}") { id: String ->
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

            val article = ArticleEntity(
                id = UUID.fromString(id),
                title = "hello",
                content = "bla bla bla",
                description = "A super article",
                createdBy = citizen
            )
            get<ArticleRepository>().upsert(article)
        }

        Given("I have article with id {string} created by {string}") { id: String, username: String ->
            val citizen = get<CitizenRepository>().findByUsername(username)!!

            val article = ArticleEntity(
                id = UUID.fromString(id),
                title = "hello",
                content = "bla bla bla",
                description = "A super article",
                createdBy = citizen
            )
            get<ArticleRepository>().upsert(article)
        }
    }
}