package fr.dcproject.application

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
import fr.dcproject.component.auth.PasswordlessAuth
import fr.dcproject.component.auth.UserRepository
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.citizen.CitizenVoter
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.component.workgroup.WorkgroupVoter
import fr.dcproject.event.publisher.Publisher
import fr.dcproject.messages.Mailer
import fr.dcproject.messages.NotificationEmailSender
import fr.dcproject.repository.CommentConstitutionRepository
import fr.dcproject.security.voter.*
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

@KtorExperimentalAPI
val KoinModule = module {

    single { Configuration }

    // SQL connection
    single {
        Connection(
            host = Configuration.host,
            port = Configuration.port,
            database = Configuration.database,
            username = Configuration.username,
            password = Configuration.password
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
    single { WorkgroupVoter() }
    single { ConstitutionVoter() }
    single { VoteVoter() }
    single { FollowVoter() }
    single { OpinionVoter() }
    single { OpinionChoiceVoter() }

    // Elasticsearch Client
    single<RestClient> {
        RestClient.builder(
            HttpHost.create(Configuration.elasticsearch)
        ).build()
    }

    single { ArticleViewManager(get()) }

    // Mailer
    single { Mailer(Configuration.sendGridKey) }

    // Used to send a connexion link by email
    single { PasswordlessAuth(get<Mailer>(), Configuration.domain, get()) }

    single { Publisher(get(), get()) }

    single { NotificationEmailSender(get<Mailer>(), Configuration.domain, get(), get()) }
}
