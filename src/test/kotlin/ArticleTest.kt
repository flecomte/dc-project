import fr.dcproject.entity.Article
import fr.dcproject.entity.CitizenBasic
import fr.dcproject.entity.CitizenI
import fr.dcproject.entity.User
import fr.postgresjson.serializer.deserialize
import fr.postgresjson.serializer.serialize
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import org.amshove.kluent.`should be equal to`
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
      "id": "83b0b60a-5ab3-44f2-b243-1dc469a7564f",
      "title": "Hello world!",
      "anonymous": true,
      "content": "bla bla bla",
      "description": "this is the changement !",
      "tags": [],
      "draft": false,
      "last_version": false,
      "created_by": {
        "id": "94a0d350-7eab-4a6e-9f84-0c2e7635b67c",
        "name": {
          "first_name": "Jaque",
          "last_name": "Bono",
          "civility": null
        },
        "email": "jaque.bono@gmail.com",
        "birthday": "2020-03-16T01:48:27.020Z",
        "vote_anonymous": true,
        "follow_anonymous": true,
        "user": {
          "id": "2bc356a2-4d3e-46ff-91f4-ae30fb7fa67d",
          "username": "jaque",
          "blocked_at": null,
          "plain_password": "azerty",
          "roles": [],
          "created_at": "2020-03-16T01:48:24.153Z",
          "updated_at": "2020-03-16T01:48:24.516Z"
        },
        "deleted_at": null,
        "deleted": false
      },
      "reference": "article",
      "views": {
        "total": 0,
        "unique": 0,
        "updated_at": "2020-03-16T01:48:31.070Z"
      },
      "version_id": "27cb4f5d-d425-4e10-95ca-6c50fac73408",
      "version_number": null,
      "created_at": "2020-03-16T01:48:31.004Z",
      "deleted_at": null,
      "deleted": false,
      "votes": {
        "up": 0,
        "neutral": 0,
        "down": 0,
        "total": 0,
        "score": 0,
        "updated_at": null
      },
      "opinions": {}
    }
    """.trimIndent()

    @Test
    fun `test Article serialize`() {
        val user = User(username = "jaque", plainPassword = "azerty")
        val citizen = CitizenBasic(
            name = CitizenI.Name("Jaque", "Bono"),
            birthday = DateTime.now(),
            email = "jaque.bono@gmail.com",
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
        article2.id.toString() `should be equal to` "83b0b60a-5ab3-44f2-b243-1dc469a7564f"
    }
}
