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

        Given("I have article with ID {string}") { id: String ->
            createArticle(id = UUID.fromString(id))
        }

        Given("I have comment created by {word} {word} on article {string}:") { firstName: String, lastName: String, articleId: String, extraData: DataTable? ->
            commentArticle(articleId, firstName, lastName, extraData)
        }
        Given("I have comment created by {word} {word} on article {string}") { firstName: String, lastName: String, articleId: String ->
            commentArticle(articleId, firstName, lastName)
        }
    }

    private fun createArticle(extraData: DataTable? = null, id: UUID? = null) {
        val params = extraData?.asMap<String, String>(String::class.java, String::class.java)
        val createdByUsername = params?.get("createdBy")
        val username = (createdByUsername ?: "username" + UUID.randomUUID().toString())
            .toLowerCase().replace(' ', '-')

        val createdBy = if (createdByUsername != null) {
            get<CitizenRepository>().findByUsername(username) ?: error("Citizen not exist")
        } else {
            val first = "firstName" + UUID.randomUUID().toString()
            val last = "lastName" + UUID.randomUUID().toString()
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
            id = id ?: params?.get("id")?.toUUID() ?: UUID.randomUUID(),
            title = "hello",
            content = "bla bla bla",
            description = "A super article",
            createdBy = createdBy
        )
        get<ArticleRepository>().upsert(article)
    }

    private fun commentArticle(articleId: String, firstName: String, lastName: String, extraData: DataTable? = null) {
        val params = extraData?.asMap<String, String>(String::class.java, String::class.java)

        val article = get<ArticleRepository>().findById(UUID.fromString(articleId)) ?: error("Article not exist")

        val citizen = get<CitizenRepository>().findByUsername(
            ("$firstName-$lastName".toLowerCase()).toLowerCase().replace(' ', '-')
        ) ?: error("Citizen not exist")

        val comment: CommentEntity<ArticleRef> = CommentEntity(
            id = params?.get("id")?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
            createdBy = citizen,
            target = article,
            content = params?.get("content") ?: "hello"
        )
        get<CommentArticle>().comment(comment)
    }
}