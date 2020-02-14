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
        /**
         * @deprecated
         */
        Given("I have article with id {string}") { id: String ->
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
                id = UUID.fromString(id),
                title = "hello",
                content = "bla bla bla",
                description = "A super article",
                createdBy = citizen
            )
            get<ArticleRepository>().upsert(article)
        }

        Given("I have article") { extraData: DataTable ->
            extraData.asMap<String, String>(String::class.java, String::class.java).let { params ->
                val username = params["createdBy"]?.toLowerCase()?.replace(' ', '-') ?: error("You must provide the 'createdBy' parameter")
                val citizen = get<CitizenRepository>().findByUsername(username) ?: error("Citizen not exist")
                val id = params["id"]?.toUUID() ?: UUID.randomUUID()
                val article = ArticleEntity(
                    id = id,
                    title = "hello",
                    content = "bla bla bla",
                    description = "A super article",
                    createdBy = citizen
                )
                get<ArticleRepository>().upsert(article)
            }

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
}