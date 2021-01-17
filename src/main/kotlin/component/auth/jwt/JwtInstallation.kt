package component.auth.jwt

import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserRepository
import fr.dcproject.component.auth.jwt.JwtConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.auth.*
import io.ktor.routing.*
import java.util.*

fun jwtInstallation(userRepo: UserRepository): Authentication.Configuration.() -> Unit = {
    /**
     * Setup the JWT authentication to be used in [Routing].
     * If the token is valid, the corresponding [User] is fetched from the database.
     * The [User] can then be accessed in each [ApplicationCall].
     */
    jwt {
        verifier(JwtConfig.verifier)
        realm = "dc-project.fr"
        validate {
            it.payload.getClaim("id").asString()?.let { id ->
                userRepo.findById(UUID.fromString(id))
            }
        }
    }

    jwt("url") {
        verifier(JwtConfig.verifier)
        realm = "dc-project.fr"
        authHeader { call ->
            call.request.queryParameters["token"]?.let {
                HttpAuthHeader.Single("Bearer", it)
            }
        }
        validate {
            it.payload.getClaim("id").asString()?.let { id ->
                userRepo.findById(UUID.fromString(id))
            }
        }
    }
}