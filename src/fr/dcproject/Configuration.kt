package fr.dcproject

import com.typesafe.config.ConfigFactory

class Config {
    private var config = ConfigFactory.load()
    val envName: String = config.getString("app.envName")

    val host: String = config.getString("db.host")
    val database: String = config.getString("db.database")
    val username: String = config.getString("db.username")
    val password: String = config.getString("db.password")
    val port: Int = config.getInt("db.port")
}
