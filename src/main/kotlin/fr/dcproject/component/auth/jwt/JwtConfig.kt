package fr.dcproject.component.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

class JwtConfig(
    private val secret: String,
    val issuer: String,
    private val validityInMs: Int,
) {
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
