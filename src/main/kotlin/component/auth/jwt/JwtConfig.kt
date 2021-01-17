package fr.dcproject.component.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val secret = "zAP5MBA4B4Ijz0MZaS48"
    const val issuer = "dc-project.fr"
    private const val validityInMs = 3_600_000 * 10 // 10 hours

    // TODO change to RSA512
    val algorithm: Algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}