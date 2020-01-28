package fr.dcproject

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import fr.dcproject.entity.UserI
import org.eclipse.jetty.util.resource.JarResource
import java.io.File
import java.util.*

class Config {
    private var config = ConfigFactory.load()

    val sqlFiles: File = try {
        File(this::class.java.getResource("/sql").toURI())
    } catch (e: IllegalArgumentException) {
        JarResource.newResource("./resources/sql").file
    }

    val envName: String = config.getString("app.envName")
    val domain: String = config.getString("app.domain")

    val host: String = config.getString("db.host")
    var database: String = config.getString("db.database")
    var username: String = config.getString("db.username")
    var password: String = config.getString("db.password")
    val port: Int = config.getInt("db.port")

    val sendGridKey: String = config.getString("mail.sendGrid.key")
}

object JwtConfig {
    private const val secret = "zAP5MBA4B4Ijz0MZaS48"
    const val issuer = "dc-project.fr"
    private const val validityInMs = 3_600_000 * 10 // 10 hours

    // TODO change to RSA512
    val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Produce a token for this combination of User and Account
     */
    fun makeToken(user: UserI): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("id", user.id.toString())
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}