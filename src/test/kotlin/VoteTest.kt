import fr.dcproject.entity.Article
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.User
import fr.dcproject.entity.Vote
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
class VoteTest {
    @Language("JSON")
    private val voteJson: String = """
    {
      "id": "032acc3d-e8c5-4cb2-9297-bec913ff8d9b",
      "created_by": {
        "id": "40c65a43-f9f8-45cd-aa31-953376e7c94a",
        "name": {
          "first_name": "Jaque",
          "last_name": "Bono",
          "civility": null
        },
        "birthday": "2019-10-01T10:59:40.570Z",
        "user_id": null,
        "vote_anonymous": true,
        "follow_anonymous": true,
        "user": {
          "id": "f68df389-fb0d-423e-90fd-a140a9ed29b9",
          "username": "jaque",
          "blocked_at": null,
          "plain_password": "azerty",
          "roles": [],
          "created_at": null,
          "updated_at": null
        },
        "deleted": false,
        "created_at": null,
        "deleted_at": null
      },
      "target": {
        "id": "90f28912-7bd5-4f37-a0ea-8620e3817d51",
        "title": "Hello world!",
        "anonymous": true,
        "content": "bla bla bla",
        "description": "this is the changement !",
        "tags": [],
        "draft": false,
        "last_version": false,
        "created_by": {
          "id": "40c65a43-f9f8-45cd-aa31-953376e7c94a",
          "name": {
            "first_name": "Jaque",
            "last_name": "Bono",
            "civility": null
          },
          "birthday": "2019-10-01T10:59:40.570Z",
          "user_id": null,
          "vote_anonymous": true,
          "follow_anonymous": true,
          "user": {
            "id": "f68df389-fb0d-423e-90fd-a140a9ed29b9",
            "username": "jaque",
            "blocked_at": null,
            "plain_password": "azerty",
            "roles": [],
            "created_at": null,
            "updated_at": null
          },
          "deleted": false,
          "created_at": null,
          "deleted_at": null
        },
        "votes": {
          "up": 0,
          "neutral": 0,
          "down": 0,
          "updated_at": null
        },
        "version_id": "48dad61e-c54b-4f4c-9f66-428f90b94045",
        "version_number": null,
        "deleted": false,
        "created_at": null,
        "deleted_at": null
      },
      "note": -1,
      "anonymous": true,
      "updated_at": null,
      "created_at": null
    }""".trimIndent()

    @Test
    fun `test Vote Article serialize`() {
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
        val vote = Vote(
            createdBy = citizen,
            target = article,
            note = -1
        )
        vote.serialize().contains("""Hello world!""") shouldBe true
        vote.serialize().contains("-1") shouldBe true
        println(vote.serialize())
    }

    @Test
    fun `test Vote Article Deserialize`() {
        val vote: Vote<Article> = voteJson.deserialize()!!
        vote.id.toString() `should equal` "032acc3d-e8c5-4cb2-9297-bec913ff8d9b"
        vote.note.toString() `should equal` "-1"
    }
}