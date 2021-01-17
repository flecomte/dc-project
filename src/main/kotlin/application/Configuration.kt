package fr.dcproject.application

import com.typesafe.config.ConfigFactory
import java.net.URI

object Configuration {
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
