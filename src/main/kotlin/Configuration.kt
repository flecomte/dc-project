package fr.dcproject

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import fr.dcproject.entity.UserI
import java.util.*
import java.net.URI

object Config {
    private var config = ConfigFactory.load()

    object Sql {
        val migrationFiles: URI = this::class.java.getResource("/sql/migrations").toURI()
        val functionFiles: URI = this::class.java.getResource("/sql/functions").toURI()
        val fixtureFiles: URI = this::class.java.getResource("/sql/fixtures").toURI()
    }

    val envName: String = config.getString("app.envName")
    val domain: String = config.getString("app.domain")

    val host: String = config.getString("db.host")
    var database: String = config.getString("db.database")
    var username: String = config.getString("db.username")
    var password: String = config.getString("db.password")
    val port: Int = config.getInt("db.port")
    val redis: String = config.getString("redis.connection")
    val elasticsearch: String = config.getString("elasticsearch.connection")
    val rabbitmq: String = config.getString("rabbitmq.connection")
    val exchangeNotificationName = "notification"
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