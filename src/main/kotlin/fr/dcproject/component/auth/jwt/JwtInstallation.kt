package fr.dcproject.component.auth.jwt

import fr.dcproject.component.auth.database.User
import fr.dcproject.component.auth.database.UserRepository
import io.ktor.application.ApplicationCall
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.routing.Routing
import java.util.UUID

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

    /* Token in URL */
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
