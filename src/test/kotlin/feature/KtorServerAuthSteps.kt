package feature

import com.auth0.jwt.JWT
import fr.dcproject.JwtConfig
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.User
import fr.postgresjson.connexion.Requester
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import io.ktor.http.HttpHeaders
import org.joda.time.DateTime
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import java.util.*
import java.util.concurrent.CompletionException
import kotlin.random.Random
import fr.dcproject.repository.Citizen as CitizenRepository

class KtorServerAuthSteps: En, KoinTest {
    private val requester: Requester  by inject()
    init {
        When("I have citizen:") { body: DataTable ->
            val user = User(username = "jaque_${Random.nextInt(0, 10000)}", plainPassword = "azerty")
            requester
                .getFunction("insert_user")
                .selectOne(user)

            val data = body.asMap<String, String>(String::class.java, String::class.java)
            val citizen = Citizen(
                id = UUID.fromString(data["id"]),
                name = Citizen.Name(data["firstName"], data["lastName"]),
                email = data["email"] ?: ((data["firstName"] + "-" + data["lastName"]).toLowerCase()) + "@dc-project.com",
                birthday = DateTime.now(),
                user = user
            )
            requester
                .getFunction("upsert_citizen")
                .selectOne(citizen)
        }

        Given("I am authenticated as {word} {word} with id {string}") { firstName: String, lastName: String, id: String ->
            val jwtAsString: String = JWT.create()
                .withIssuer("dc-project.fr")
                .withClaim("id", id)
                .sign(JwtConfig.algorithm)

            val user = User(
                id = UUID.fromString(id),
                username = "$firstName-$lastName".toLowerCase(),
                plainPassword = "azerty"
            )
            val citizen = Citizen(
                id = UUID.fromString(id),
                name = Citizen.Name(firstName, lastName),
                email = ("$firstName-$lastName".toLowerCase())+"@dc-project.fr",
                birthday = DateTime.now(),
                user = user
            )

            try {
                get<CitizenRepository>().insertWithUser(citizen)
            } catch (e: CompletionException) {
                // Nothing
            }

            KtorServerContext.defaultServer.addPreRequestSetup {
                addHeader(HttpHeaders.Authorization, "Bearer $jwtAsString")
            }
        }

        Given("I have citizen {word} {word} with id {string}") { firstName: String, lastName: String, id: String ->
            val user = User(
                id = UUID.randomUUID(),
                username = "$firstName-$lastName".toLowerCase(),
                plainPassword = "azerty"
            )
            val citizen = Citizen(
                id = UUID.fromString(id),
                name = Citizen.Name(firstName, lastName),
                email = "$firstName-$lastName".toLowerCase() + "@gmail.com",
                birthday = DateTime.now(),
                user = user,
                followAnonymous = false,
                voteAnonymous = false
            )

            try {
                get<CitizenRepository>().insertWithUser(citizen)
            } catch (e: CompletionException) {
                // Nothing
            }
        }
    }
}