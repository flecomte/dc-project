package feature

import fr.dcproject.entity.*
import fr.dcproject.repository.CommentArticle
import fr.dcproject.utils.toUUID
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import org.joda.time.DateTime
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*
import java.util.concurrent.CompletionException
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.entity.User as UserEntity
import fr.dcproject.repository.Article as ArticleRepository
import fr.dcproject.repository.Citizen as CitizenRepository

class ArticleSteps : En, KoinTest {
    init {
        Given("I have {int} article") { nb: Int ->
            repeat(nb) {
                createArticle()
            }
        }

        Given("I have article") { extraData: DataTable? ->
            createArticle(extraData)
        }

        Given("I have comment {string} on article {string}") { commentId: String, articleId: String ->
            var citizen = Citizen(
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

            val article = ArticleEntity(
                id = UUID.fromString(articleId),
                title = "hello",
                content = "bla bla bla",
                description = "A super article",
                createdBy = citizen
            )
            get<ArticleRepository>().upsert(article)

            val comment: CommentEntity<ArticleRef> = CommentEntity(
                id = UUID.fromString(commentId),
                createdBy = citizen,
                target = article,
                content = "hello"
            )
            get<CommentArticle>().comment(comment)
        }
    }

    private fun createArticle(extraData: DataTable? = null) {
        val params = extraData?.asMap<String, String>(String::class.java, String::class.java)
        val createdByUsername = params?.get("createdBy")
        val username = (createdByUsername ?: UUID.randomUUID().toString())
            .toLowerCase().replace(' ', '-')

        val createdBy = if (createdByUsername != null) {
            get<CitizenRepository>().findByUsername(username) ?: error("Citizen not exist")
        } else {
            val first = "firstName"+UUID.randomUUID().toString()
            val last = "lastName"+UUID.randomUUID().toString()
            Citizen(
                birthday = DateTime.now(),
                name = CitizenI.Name(
                    first,
                    last
                ),
                email = "$first@fakeemail.com",
                user = UserEntity(username = username, plainPassword = "azerty")
            ).also {
                get<CitizenRepository>().insertWithUser(it)
            }
        }

        val article = ArticleEntity(
            id = params?.get("id")?.toUUID() ?: UUID.randomUUID(),
            title = "hello",
            content = "bla bla bla",
            description = "A super article",
            createdBy = createdBy
        )
        get<ArticleRepository>().upsert(article)
    }
}