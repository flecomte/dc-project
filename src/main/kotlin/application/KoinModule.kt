package fr.dcproject.application

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRepository
import fr.dcproject.component.article.ArticleViewManager
import fr.dcproject.component.auth.PasswordlessAuth
import fr.dcproject.component.auth.UserRepository
import fr.dcproject.component.citizen.CitizenAccessControl
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.constitution.CommentConstitutionRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.constitution.ConstitutionAccessControl
import fr.dcproject.component.constitution.ConstitutionRepository
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.opinion.OpinionAccessControl
import fr.dcproject.component.opinion.OpinionChoiceAccessControl
import fr.dcproject.component.opinion.OpinionChoiceRepository
import fr.dcproject.component.vote.VoteAccessControl
import fr.dcproject.component.vote.VoteArticleRepository
import fr.dcproject.component.vote.VoteCommentRepository
import fr.dcproject.component.vote.VoteConstitutionRepository
import fr.dcproject.component.vote.VoteRepository
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.WorkgroupRepository
import fr.dcproject.event.publisher.Publisher
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
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import fr.dcproject.component.comment.generic.CommentRepository as CommentGenericRepository
import fr.dcproject.component.follow.FollowArticleRepository as FollowArticleRepository
import fr.dcproject.component.follow.FollowConstitutionRepository as FollowConstitutionRepository
import fr.dcproject.component.opinion.OpinionRepositoryArticle as OpinionArticleRepository

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
    single { VoteRepository(get()) }
    single { VoteArticleRepository(get()) }
    single { VoteConstitutionRepository(get()) }
    single { VoteCommentRepository(get()) }
    single { OpinionChoiceRepository(get()) }
    single { OpinionArticleRepository(get()) }
    single { WorkgroupRepository(get()) }

    // AccessControl
    single { ArticleAccessControl(get()) }
    single { CitizenAccessControl() }
    single { CommentAccessControl() }
    single { WorkgroupAccessControl() }
    single { ConstitutionAccessControl() }
    single { VoteAccessControl() }
    single { FollowAccessControl() }
    single { OpinionAccessControl() }
    single { OpinionChoiceAccessControl() }

    // Elasticsearch Client
    single<RestClient> {
        RestClient.builder(
            HttpHost.create(Configuration.elasticsearch)
        ).build()
    }

    single { ArticleViewManager<ArticleForView>(get()) }

    // Mailer
    single { Mailer(Configuration.sendGridKey) }

    // Used to send a connexion link by email
    single { PasswordlessAuth(get<Mailer>(), Configuration.domain, get()) }

    single { Publisher(get(), get(), exchangeName = Configuration.exchangeNotificationName) }

    single { NotificationEmailSender(get<Mailer>(), Configuration.domain, get(), get()) }
}
