package fr.dcproject

import com.auth0.jwt.JWT
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.auth.jwt.JwtConfig

/**
 * Produce a token for this combination of User and Account
 */
fun UserI.makeToken(): String = JWT.create()
    .withSubject("Authentication")
    .withIssuer(JwtConfig.issuer)
    .withClaim("id", id.toString())
    .withExpiresAt(JwtConfig.getExpiration())
    .sign(JwtConfig.algorithm)