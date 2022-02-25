package fr.dcproject.application

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.common.email.Mailer
import fr.dcproject.component.auth.jwt.JwtConfig
import fr.dcproject.component.notification.NotificationPublisherAsync
import fr.dcproject.component.notification.email.NotificationEmailConsumer
import fr.dcproject.component.notification.email.NotificationEmailSender
import fr.dcproject.component.notification.push.NotificationPushConsumer
import fr.dcproject.component.notification.push.NotificationPushListener
import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.lettuce.core.RedisClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val KoinModule = module {
    // JWT
    single {
        val config: Configuration = get()
        JwtConfig(
            config.jwt.secret,
            config.jwt.issuer,
            config.jwt.validityInMs,
        )
    }
    // JWT Verifier
    single {
        get<JwtConfig>().verifier
    }
    // SQL connection
    single {
        val config: Configuration = get()
        Connection(
            host = config.database.host,
            port = config.database.port,
            database = config.database.database,
            username = config.database.username,
            password = config.database.password
        )
    }

    // Launch Database migration
    single {
        val config: Configuration = get()
        Migrations(get(), config.sql.migrationFiles, config.sql.functionFiles)
    }

    // Redis client
    single<RedisClient> {
        val config: Configuration = get()
        RedisClient.create(config.redis).apply {
            connect().sync().configSet("notify-keyspace-events", "KEA")
        }
    }

    single { NotificationPushListener.Builder(get()) }

    single {
        val config: Configuration = get()
        NotificationEmailConsumer(get(), get(), get(), get(), get(), config.exchangeNotificationName)
    }
    single {
        val config: Configuration = get()
        NotificationPushConsumer(get(), get(), get(), get(), get(), config.exchangeNotificationName)
    }

    // RabbitMQ
    single<ConnectionFactory> {
        val config: Configuration = get()
        ConnectionFactory().apply { setUri(config.rabbitmq) }
    }

    // JsonSerializer
    single<ObjectMapper> {
        jacksonObjectMapper().apply {
            registerModule(SimpleModule())
            propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE

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
        val config: Configuration = get()
        Requester.RequesterFactory(
            connection = get(),
            functionsDirectory = config.sql.functionFiles
        ).createRequester()
    }

    // Mailer
    single {
        val config: Configuration = get()
        Mailer(config.sendGridKey)
    }

    single {
        val config: Configuration = get()
        NotificationPublisherAsync(factory = get(), exchangeName = config.exchangeNotificationName)
    }

    single {
        val config: Configuration = get()
        NotificationEmailSender(get<Mailer>(), config.domain, get(), get())
    }
}
