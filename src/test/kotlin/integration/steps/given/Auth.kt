package integration.steps.given

import com.auth0.jwt.JWT
import fr.dcproject.component.auth.jwt.JwtConfig
import fr.dcproject.component.citizen.database.Citizen
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRepository
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationRequest
import org.koin.core.context.GlobalContext

fun TestApplicationRequest.`authenticated as`(
    firstName: String,
    lastName: String,
): Citizen {
    val username = "$firstName-$lastName".lowercase()
    val repo: CitizenRepository by lazy<CitizenRepository> { GlobalContext.get().get() }
    val citizen = repo.findByUsername(username) ?: error("Citizen not exist with username $username")
    val algorithm = GlobalContext.get().get<JwtConfig>().algorithm
    val jwtAsString: String = JWT.create()
        .withIssuer("dc-project.fr")
        .withClaim("id", citizen.user.id.toString())
        .sign(algorithm)

    addHeader(HttpHeaders.Authorization, "Bearer $jwtAsString")

    return citizen
}
fun TestApplicationRequest.`authenticated in url as`(
    firstName: String,
    lastName: String,
): Citizen {
    val repo: CitizenRepository by lazy<CitizenRepository> { GlobalContext.get().get() }
    val citizen = repo.findByName(CitizenI.Name(firstName, lastName)) ?: error("Citizen not exist with name $firstName $lastName")
    val algorithm = GlobalContext.get().get<JwtConfig>().algorithm
    val jwtAsString: String = JWT.create()
        .withIssuer("dc-project.fr")
        .withClaim("id", citizen.user.id.toString())
        .sign(algorithm)

    uri += when (uri.contains('?')) {
        true -> '&'
        false -> '?'
    }
    uri += "token=$jwtAsString"

    return citizen
}
