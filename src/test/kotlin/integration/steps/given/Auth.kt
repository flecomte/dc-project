package integration.steps.given

import com.auth0.jwt.JWT
import fr.dcproject.component.auth.jwt.JwtConfig
import fr.dcproject.component.citizen.database.Citizen
import fr.dcproject.component.citizen.database.CitizenRepository
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationRequest
import org.koin.core.context.GlobalContext

fun TestApplicationRequest.`authenticated as`(
    firstName: String,
    lastName: String,
): Citizen {
    val username = "$firstName-$lastName".toLowerCase()
    val repo: CitizenRepository by lazy<CitizenRepository> { GlobalContext.get().koin.get() }
    val citizen = repo.findByUsername(username) ?: error("Citizen not exist with username $username")
    val jwtAsString: String = JWT.create()
        .withIssuer("dc-project.fr")
        .withClaim("id", citizen.user.id.toString())
        .sign(JwtConfig.algorithm)

    addHeader(HttpHeaders.Authorization, "Bearer $jwtAsString")

    return citizen
}
