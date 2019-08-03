import fr.dcproject.entity.Article
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.User
import fr.postgresjson.serializer.deserialize
import fr.postgresjson.serializer.serialize
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldBe
import org.intellij.lang.annotations.Language
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@TestInstance(PER_CLASS)
class ArticleTest {
    @Language("JSON")
    private val articleJson: String = """
    {
      "id" : "83b0b60a-5ab3-44f2-b243-1dc469a7564f",
      "version_id" : "fff2311c-07cc-43a6-bab1-aec6b649a903",
      "version_number" : null,
      "title" : "Hello world!",
      "annonymous" : true,
      "content" : "bla bla bla",
      "description" : "this is the changement !",
      "tags" : [ ],
      "created_by" : {
        "id" : "3fff09e4-5ff2-46ee-9fd2-3803a1ffb600",
        "name" : {
          "first_name" : "Jaque",
          "last_name" : "Bono",
          "civility" : null
        },
        "birthday" : "2019-08-03T13:43:13.765Z",
        "user_id" : null,
        "vote_annonymous" : null,
        "follow_annonymous" : null,
        "user" : {
          "id" : "151ec430-3aad-4792-9a14-e394b2be491b",
          "username" : "jaque",
          "blocked_at" : null,
          "plain_password" : "azerty",
          "created_at" : null,
          "updated_at" : null
        },
        "created_at" : null
      },
      "created_at" : null
    }
    """.trimIndent()

    @Test
    fun `test Article serialize`() {
        val user = User(username = "jaque", plainPassword = "azerty")
        val citizen = Citizen(
            name = Citizen.Name("Jaque", "Bono"),
            birthday = DateTime.now(),
            user = user
        )
        val article = Article(
            title = "Hello world!",
            content = "bla bla bla",
            description = "this is the changement !",
            createdBy = citizen
        )
        article.serialize().contains("""Hello world!""") shouldBe true
    }

    @Test
    fun `test Article Deserialize`() {
        val article2: Article = articleJson.deserialize()!!
        article2.id.toString() `should equal` "83b0b60a-5ab3-44f2-b243-1dc469a7564f"
    }
}
