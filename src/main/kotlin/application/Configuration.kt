package fr.dcproject.application

import com.typesafe.config.ConfigFactory
import java.net.URI

object Configuration {
    private var config = ConfigFactory.load()

    object Sql {
        val migrationFiles: URI = this::class.java.getResource("/sql/migrations")?.toURI() ?: error("No migrations found")
        val functionFiles: URI = this::class.java.getResource("/sql/functions")?.toURI() ?: error("No sql function found")
        val fixtureFiles: URI = this::class.java.getResource("/sql/fixtures")?.toURI() ?: error("No sql fixture found")
    }
    object Database {
        val host: String = config.getString("db.host")
        val port: Int = config.getInt("db.port")
        var database: String = config.getString("db.database")
        var username: String = config.getString("db.username")
        var password: String = config.getString("db.password")
    }

    val envName: String = config.getString("app.envName")
    val domain: String = config.getString("app.domain")

    val redis: String = config.getString("redis.connection")
    val elasticsearch: String = config.getString("elasticsearch.connection")
    val rabbitmq: String = config.getString("rabbitmq.connection")
    val exchangeNotificationName = "notification"
    val sendGridKey: String = config.getString("mail.sendGrid.key")
}
