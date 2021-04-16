package fr.dcproject.component.auth.jwt

import com.auth0.jwt.JWT
import fr.dcproject.component.auth.database.UserI
import org.koin.core.context.GlobalContext

/**
 * Produce a token for this combination of User and Account
 */
fun UserI.makeToken(): String = GlobalContext.get().get<JwtConfig>().run {
    JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("id", id.toString())
        .withExpiresAt(getExpiration())
        .sign(algorithm)
}
