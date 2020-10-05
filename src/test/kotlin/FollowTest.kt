import fr.dcproject.entity.*
import fr.postgresjson.serializer.deserialize
import fr.postgresjson.serializer.serialize
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import org.amshove.kluent.`should be equal to`
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
class FollowTest {
    @Language("JSON")
    private val followJson: String = """{  
       "id":"bae81585-d985-4d7a-9b58-3a13e911688a",
       "created_by":{  
          "id":"4a87ad24-187a-46a8-97ab-00b30a24e561",
          "name":{  
             "first_name":"Jaque",
             "last_name":"Bono",
             "civility":null
          },
          "email": "jaque.bono@gmail.com",
          "birthday":"2019-08-09T11:42:47.168Z",
          "user_id":null,
          "vote_anonymous":null,
          "follow_anonymous":null,
          "user":{  
             "id":"721db690-d050-46e6-92b0-056f2e8ba993",
             "username":"jaque",
             "blocked_at":null,
             "plain_password":"azerty",
             "created_at":"2019-08-09T11:42:47.168Z",
             "updated_at":"2019-08-09T11:42:47.168Z"
          },
          "created_at":"2019-08-09T11:42:47.168Z"
       },
       "target":{  
          "id":"34588ea7-c180-4694-801b-1b5c5a6ed73f",
          "title":"Hello world!",
          "anonymous":true,
          "content":"bla bla bla",
          "description":"this is the changement !",
          "tags":[  
    
          ],
          "created_by":{
             "id":"4a87ad24-187a-46a8-97ab-00b30a24e561",
             "name":{  
                "first_name":"Jaque",
                "last_name":"Bono",
                "civility":null
             },
             "email": "jaque.bono@gmail.com",
             "birthday":"2019-08-09T11:42:47.168Z",
             "user_id":null,
             "vote_anonymous":null,
             "follow_anonymous":null,
             "user":{  
                "id":"721db690-d050-46e6-92b0-056f2e8ba993",
                "username":"jaque",
                "blocked_at":null,
                "plain_password":"azerty",
                "created_at":"2019-08-09T11:42:47.168Z",
                "updated_at":"2019-08-09T11:42:47.168Z"
             }
          },
          "version_id":"a4aa7dd4-d174-42d2-9ba5-ae6f1129ffce",
          "version_number":null,
          "created_at":null
       },
       "created_at":"2019-08-09T11:42:47.168Z"
    }""".trimIndent()

    @Test
    fun `test Follow Article serialize`() {
        val user = User(username = "jaque", plainPassword = "azerty")
        val citizen = CitizenBasic(
            name = CitizenI.Name("Jaque", "Bono"),
            email = "jaque.bono@gmail.com",
            birthday = DateTime.now(),
            user = user
        )
        val article = Article(
            title = "Hello world!",
            content = "bla bla bla",
            description = "this is the changement !",
            createdBy = citizen
        )
        val follow = Follow(
            createdBy = citizen,
            target = article
        )
        follow.serialize().contains("""Hello world!""") shouldBe true
        println(follow.serialize())
    }

    @Test
    fun `test Follow Article Deserialize`() {
        val follow: Follow<ArticleSimple> = followJson.deserialize()!!
        follow.id.toString() `should be equal to` "bae81585-d985-4d7a-9b58-3a13e911688a"
    }
}
