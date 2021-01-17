package steps

import com.auth0.jwt.JWT
import fr.dcproject.component.auth.jwt.JwtConfig
import fr.dcproject.component.citizen.CitizenRepository
import io.cucumber.java8.En
import io.ktor.http.*
import org.koin.test.KoinTest
import org.koin.test.get

class KtorServerAuthSteps : En, KoinTest {
    init {
        Given("I am authenticated as {word} {word}") { firstName: String, lastName: String ->
            val username = "$firstName-$lastName".toLowerCase()
            val citizen = get<CitizenRepository>().findByUsername(username) ?: error("Cititzen not exist with username $username")
            val jwtAsString: String = JWT.create()
                .withIssuer("dc-project.fr")
                .withClaim("id", citizen.user.id.toString())
                .sign(JwtConfig.algorithm)

            KtorServerContext.defaultServer.addPreRequestSetup {
                addHeader(HttpHeaders.Authorization, "Bearer $jwtAsString")
            }
        }
    }
}