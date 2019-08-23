package feature

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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
import kotlin.random.Random
import fr.dcproject.repository.User as UserRepository

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
                birthday = DateTime.now(),
                user = user
            )
            requester
                .getFunction("upsert_citizen")
                .selectOne(citizen)
        }

        Given("I am authenticated as an user") {
            val id = UUID.randomUUID()
            val jwtAsString: String = JWT.create()
                .withIssuer("dc-project.fr")
                .withClaim("id", id.toString())
                .sign(Algorithm.HMAC512(JwtConfig.secret))

            val user = User(id = id, username = "user", plainPassword = "azerty")
            get<UserRepository>().insert(user)

            KtorServerContext.defaultServer.addPreRequestSetup {
                addHeader(HttpHeaders.Authorization, "Bearer $jwtAsString")
            }
        }
    }
}