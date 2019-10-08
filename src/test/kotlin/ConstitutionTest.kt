import fr.dcproject.entity.Citizen
import fr.dcproject.entity.Constitution
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
class ConstitutionTest {
    @Language("JSON")
    private val constitutionJson: String = """{  
       "id":"15814bb6-8d90-4c6a-a456-c3939a8ec75e",
       "title":"Hello world!",
       "anonymous":true,
       "titles":[  
          {  
             "id":"8156b66f-a9c8-4fd9-8375-a8a1f42ccfd2",
             "name":"plop",
             "rank":0,
             "created_by":{  
                "id":"18902d22-245d-4d44-b23d-9f0e82688612",
                "name":{  
                   "first_name":"Jaque",
                   "last_name":"Bono",
                   "civility":null
                },
                "email": "jaque.bono@gmail.com",
                "birthday":"2019-08-07T20:34:08.013Z",
                "user_id":null,
                "vote_anonymous":null,
                "follow_anonymous":null,
                "user":{  
                   "id":"257abe9f-be17-4ad3-ae6a-b1dc9706d5d7",
                   "username":"jaque",
                   "blocked_at":null,
                   "plain_password":"azerty",
                   "created_at":null,
                   "updated_at":null
                },
                "created_at":null
             },
             "created_at":null
          }
       ],
       "created_by":{  
          "id":"18902d22-245d-4d44-b23d-9f0e82688612",
          "name":{  
             "first_name":"Jaque",
             "last_name":"Bono",
             "civility":null
          },
          "email": "jaque.bono@gmail.com",
          "birthday":"2019-08-07T20:34:08.013Z",
          "user_id":null,
          "vote_anonymous":null,
          "follow_anonymous":null,
          "user":{  
             "id":"257abe9f-be17-4ad3-ae6a-b1dc9706d5d7",
             "username":"jaque",
             "plain_password":"azerty"
          }
       },
       "created_at":null,
       "version_id":"3311a7af-2a62-4e31-b4cd-889f8ead9737",
       "version_number":null
    }""".trimIndent()

    @Test
    fun `test Constitution serialize`() {
        val user = User(username = "jaque", plainPassword = "azerty")
        val citizen = Citizen(
            name = Citizen.Name("Jaque", "Bono"),
            email = "jaque.bono@gmail.com",
            birthday = DateTime.now(),
            user = user
        )
        val title1 = Constitution.Title(
            name = "plop"
        )
        val constitution = Constitution(
            title = "Hello world!",
            anonymous = true,
            titles = listOf(title1),
            createdBy = citizen
        )
        println(constitution.serialize())
        constitution.serialize().contains("""Hello world!""") shouldBe true
    }

    @Test
    fun `test Constitution Deserialize`() {
        val constitution2: Constitution = constitutionJson.deserialize()!!
        constitution2.id.toString() `should equal` "15814bb6-8d90-4c6a-a456-c3939a8ec75e"
    }
}
