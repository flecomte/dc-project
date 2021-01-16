package fr.dcproject

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.article.ArticleViewManager
import fr.dcproject.component.article.ArticleVoter
import fr.dcproject.component.auth.SsoManager
import fr.dcproject.component.auth.UserRepository
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.event.publisher.Publisher
import fr.dcproject.messages.Mailer
import fr.dcproject.messages.NotificationEmailSender
import fr.dcproject.repository.CommentConstitutionRepository
import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.util.*
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import fr.dcproject.component.comment.generic.CommentRepository as CommentGenericRepository
import fr.dcproject.repository.Constitution as ConstitutionRepository
import fr.dcproject.repository.FollowArticle as FollowArticleRepository
import fr.dcproject.repository.FollowConstitution as FollowConstitutionRepository
import fr.dcproject.repository.OpinionArticle as OpinionArticleRepository
import fr.dcproject.repository.OpinionChoice as OpinionChoiceRepository
import fr.dcproject.repository.VoteArticle as VoteArticleRepository
import fr.dcproject.repository.VoteComment as VoteCommentRepository
import fr.dcproject.repository.VoteConstitution as VoteConstitutionRepository
import fr.dcproject.repository.Workgroup as WorkgroupRepository

@KtorExperimentalAPI
val KoinModule = module {

    single { Config }

    // SQL connection
    single {
        Connection(
            host = Config.host,
            port = Config.port,
            database = Config.database,
            username = Config.username,
            password = Config.password
        )
    }

    // Launch Database migration
    single { Migrations(get(), Config.Sql.migrationFiles, Config.Sql.functionFiles) }

    // Redis client
    single<RedisAsyncCommands<String, String>> {
        RedisClient.create(Config.redis).connect()?.async() ?: error("Unable to connect to redis")
    }

    // RabbitMQ
    single<ConnectionFactory> {
        ConnectionFactory().apply { setUri(Config.rabbitmq) }
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
            functionsDirectory = Config.Sql.functionFiles
        ).createRequester()
    }

    // Repositories
    single { UserRepository(get()) }
    single { ArticleRepository(get()) }
    single { CitizenRepository(get()) }
    single { ConstitutionRepository(get()) }
    single { FollowArticleRepository(get()) }
    single { FollowConstitutionRepository(get()) }
    single { CommentGenericRepository(get()) }
    single { CommentArticleRepository(get()) }
    single { CommentConstitutionRepository(get()) }
    single { VoteArticleRepository(get()) }
    single { VoteConstitutionRepository(get()) }
    single { VoteCommentRepository(get()) }
    single { OpinionChoiceRepository(get()) }
    single { OpinionArticleRepository(get()) }
    single { WorkgroupRepository(get()) }

    // Voters
    single { ArticleVoter(get()) }
    single { CitizenVoter() }
    single { CommentVoter() }

    // Elasticsearch Client
    single<RestClient> {
        RestClient.builder(
            HttpHost.create(Config.elasticsearch)
        ).build()
    }

    single { ArticleViewManager(get()) }

    // Mailer
    single { Mailer(Config.sendGridKey) }

    // SSO Manager for connection
    single { SsoManager(get<Mailer>(), Config.domain, get()) }

    single { Publisher(get(), get()) }

    single { NotificationEmailSender(get<Mailer>(), Config.domain, get(), get()) }
}
