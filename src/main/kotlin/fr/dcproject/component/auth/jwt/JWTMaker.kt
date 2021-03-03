package fr.dcproject.component.auth.jwt

import com.auth0.jwt.JWT
import fr.dcproject.component.auth.database.UserI

/**
 * Produce a token for this combination of User and Account
 */
fun UserI.makeToken(): String = JWT.create()
    .withSubject("Authentication")
    .withIssuer(JwtConfig.issuer)
    .withClaim("id", id.toString())
    .withExpiresAt(JwtConfig.getExpiration())
    .sign(JwtConfig.algorithm)
