package integration.prerequisite

import com.auth0.jwt.JWT
import fr.dcproject.component.auth.UserForCreate
import fr.dcproject.component.auth.jwt.JwtConfig
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenForCreate
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRepository
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.get
import org.koin.test.get
import steps.KtorServerContext
import java.util.UUID

fun TestApplicationEngine.`Given I have citizen`(
    firstName: String,
    lastName: String,
    email: String = ("$firstName-$lastName".toLowerCase()) + "@dc-project.fr",
    id: UUID = UUID.randomUUID()
): Citizen? {
    val repo: CitizenRepository by lazy<CitizenRepository> { GlobalContext.get().koin.get() }

    val user = UserForCreate(
        id = id,
        username = "$firstName-$lastName".toLowerCase(),
        password = "azerty",
    )
    val citizen = CitizenForCreate(
        id = id,
        name = CitizenI.Name(firstName, lastName),
        email = email,
        birthday = DateTime.now(),
        user = user
    )

    return repo.insertWithUser(citizen)
}
