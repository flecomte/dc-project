package fr.dcproject

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.messages.Mailer
import fr.dcproject.messages.SsoManager
import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import org.koin.dsl.module
import fr.dcproject.repository.Article as ArticleRepository
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.CommentArticle as CommentArticleRepository
import fr.dcproject.repository.CommentConstitution as CommentConstitutionRepository
import fr.dcproject.repository.CommentGeneric as CommentGenericRepository
import fr.dcproject.repository.Constitution as ConstitutionRepository
import fr.dcproject.repository.FollowArticle as FollowArticleRepository
import fr.dcproject.repository.FollowConstitution as FollowConstitutionRepository
import fr.dcproject.repository.OpinionArticle as OpinionArticleRepository
import fr.dcproject.repository.OpinionChoice as OpinionChoiceRepository
import fr.dcproject.repository.User as UserRepository
import fr.dcproject.repository.VoteArticle as VoteArticleRepository
import fr.dcproject.repository.VoteComment as VoteCommentRepository
import fr.dcproject.repository.VoteConstitution as VoteConstitutionRepository

val config = Config()

@KtorExperimentalAPI
val Module = module {

    single { config }

    single {
        Connection(
            host = config.host,
            port = config.port,
            database = config.database,
            username = config.username,
            password = config.password
        )
    }

    single { Migrations(connection = get(), directory = config.sqlFiles) }

    single<RedisAsyncCommands<String, String>> {
        RedisClient.create(config.redis).connect()?.async() ?: error("Unable to connect to redis")
    }

    single<ConnectionFactory> {
        ConnectionFactory().apply { setUri(config.rabbitmq) }
    }

    single<ObjectMapper> {
        jacksonObjectMapper().apply {
            registerModule(SimpleModule())
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

            registerModule(JodaModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        }
    }

    single {
        Requester.RequesterFactory(
            connection = get(),
            functionsDirectory = config.sqlFiles.resolve("functions")
        ).createRequester()
    }

    // TODO: create generic declaration
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

    single { Mailer(config.sendGridKey) }
    single { SsoManager(get<Mailer>(), config.domain, get()) }
}
