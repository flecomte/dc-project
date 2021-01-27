package fr.dcproject.application

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.notification.publisher.Publisher
import fr.dcproject.messages.Mailer
import fr.dcproject.messages.NotificationEmailSender
import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import org.koin.core.qualifier.named
import org.koin.dsl.module

@KtorExperimentalAPI
val KoinModule = module {
    // SQL connection
    single {
        Connection(
            host = Configuration.Database.host,
            port = Configuration.Database.port,
            database = Configuration.Database.database,
            username = Configuration.Database.username,
            password = Configuration.Database.password
        )
    }

    // Launch Database migration
    single { Migrations(get(), Configuration.Sql.migrationFiles, Configuration.Sql.functionFiles) }

    // Redis client
    single<RedisAsyncCommands<String, String>> {
        RedisClient.create(Configuration.redis).connect()?.async() ?: error("Unable to connect to redis")
    }

    // RabbitMQ
    single<ConnectionFactory> {
        ConnectionFactory().apply { setUri(Configuration.rabbitmq) }
    }

    // JsonSerializer
    single<ObjectMapper> {
        jacksonObjectMapper().apply {
            registerModule(SimpleModule())
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

            registerModule(JodaModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        }
    }

    // Client HTTP for WebSockets
    single(named("ws")) {
        HttpClient {
            install(WebSockets)
        }
    }

    // SQL Requester (postgresJson)
    single {
        Requester.RequesterFactory(
            connection = get(),
            functionsDirectory = Configuration.Sql.functionFiles
        ).createRequester()
    }

    // Mailer
    single { Mailer(Configuration.sendGridKey) }

    single { Publisher(factory = get(), exchangeName = Configuration.exchangeNotificationName) }

    single { NotificationEmailSender(get<Mailer>(), Configuration.domain, get(), get()) }
}
