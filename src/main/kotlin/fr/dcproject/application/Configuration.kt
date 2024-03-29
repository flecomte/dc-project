package fr.dcproject.application

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.net.URI

class Configuration(val config: Config) {
    constructor(resourceBasename: String? = null) : this(if (resourceBasename == null) ConfigFactory.load() else ConfigFactory.load(resourceBasename))

    interface Sql {
        val migrationFiles: URI
        val functionFiles: URI
        val fixtureFiles: URI
    }
    val sql
        get() = object : Sql {
            override val migrationFiles: URI = this::class.java.getResource("/sql/migrations")?.toURI() ?: error("No migrations found")
            override val functionFiles: URI = this::class.java.getResource("/sql/functions")?.toURI() ?: error("No sql function found")
            override val fixtureFiles: URI = this::class.java.getResource("/sql/fixtures")?.toURI() ?: error("No sql fixture found")
        }

    interface Database {
        val host: String
        val port: Int
        var database: String
        var username: String
        var password: String
    }
    val database
        get() = object : Database {
            override val host: String = config.getString("db.host")
            override val port: Int = config.getInt("db.port")
            override var database: String = config.getString("db.database")
            override var username: String = config.getString("db.username")
            override var password: String = config.getString("db.password")
        }

    val envName: String = config.getString("app.envName")
    val domain: String = config.getString("app.domain")

    val redis: String = config.getString("redis.connection")
    val elasticsearch: String = config.getString("elasticsearch.connection")
    val rabbitmq: String = config.getString("rabbitmq.connection")
    val exchangeNotificationName = "notification"
    val sendGridKey: String = config.getString("mail.sendGrid.key")

    interface Jwt {
        val secret: String
        val issuer: String
        val validityInMs: Int
    }
    val jwt = object : Jwt {
        override val secret = config.getString("jwt.secret")
        override val issuer = config.getString("jwt.issuer")
        override val validityInMs = config.getInt("jwt.validity")
    }
}
