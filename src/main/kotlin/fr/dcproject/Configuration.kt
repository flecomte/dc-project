package fr.dcproject

import com.typesafe.config.ConfigFactory
import java.io.File

class Config {
    private var config = ConfigFactory.load()
    val sqlFiles = File(this::class.java.getResource("/sql").toURI())
    val envName: String = config.getString("app.envName")

    val host: String = config.getString("db.host")
    var database: String = config.getString("db.database")
    var username: String = config.getString("db.username")
    var password: String = config.getString("db.password")
    val port: Int = config.getInt("db.port")
}
