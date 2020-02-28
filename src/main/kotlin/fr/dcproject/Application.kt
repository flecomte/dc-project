package fr.dcproject

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException
import com.rabbitmq.client.*
import com.rabbitmq.client.BuiltinExchangeType.DIRECT
import fr.dcproject.Env.PROD
import fr.dcproject.entity.*
import fr.dcproject.event.EntityEvent
import fr.dcproject.event.EventNotification
import fr.dcproject.event.Notification
import fr.dcproject.event.publisher.Publisher
import fr.dcproject.repository.Follow
import fr.dcproject.repository.FollowArticle
import fr.dcproject.routes.*
import fr.dcproject.security.voter.*
import fr.postgresjson.migration.Migrations
import fr.postgresjson.serializer.deserialize
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.client.engine.jetty.Jetty
import io.ktor.features.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.websocket.WebSockets
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.eclipse.jetty.util.log.Slf4jLog
import org.koin.core.qualifier.named
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.slf4j.event.Level
import java.io.IOException
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletionException
import fr.dcproject.repository.Article as RepositoryArticle
import fr.dcproject.repository.Citizen as RepositoryCitizen
import fr.dcproject.repository.Constitution as RepositoryConstitution
import fr.dcproject.repository.OpinionChoice as OpinionChoiceRepository
import fr.dcproject.repository.User as UserRepository

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

enum class Env { PROD, TEST, CUCUMBER }

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module(env: Env = PROD) {
    install(Koin) {
        Slf4jLog()
        modules(Module)
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(DataConversion) {
        // TODO move to postgresJson lib
        convert<UUID> {
            decode { values, _ ->
                values.singleOrNull()?.let { UUID.fromString(it) }
            }

            encode { value ->
                when (value) {
                    null -> listOf()
                    is UUID -> listOf(value.toString())
                    else -> throw InternalError("Cannot convert $value as UUID")
                }
            }
        }

        // TODO: create generic convert for entityI
        convert<Article> {
            decode { values, _ ->
                values.singleOrNull()?.let {
                    get<RepositoryArticle>().findById(UUID.fromString(it))
                        ?: throw NotFoundException("Article $values not found")
                } ?: throw NotFoundException("Article $values not found")
            }
        }
        convert<ArticleRef> {
            decode { values, _ ->
                values.singleOrNull()?.let {
                    ArticleRef(UUID.fromString(it))
                } ?: throw NotFoundException("Article $values not found")
            }
        }

        convert<CommentRef> {
            decode { values, _ ->
                values.singleOrNull()?.let {
                    CommentRef(UUID.fromString(it))
                } ?: throw NotFoundException("Comment $values not found")
            }
        }
        convert<ConstitutionRef> {
            decode { values, _ ->
                values.singleOrNull()?.let {
                    ConstitutionRef(UUID.fromString(it))
                } ?: throw NotFoundException("Constitution $values not found")
            }
        }

        convert<Constitution> {
            decode { values, _ ->
                val id = values.singleOrNull()?.let { UUID.fromString(it) }
                    ?: throw InternalError("Cannot convert $values to UUID")
                get<RepositoryConstitution>().findById(id) ?: throw NotFoundException("Constitution $values not found")
            }
        }

        convert<Citizen> {
            decode { values, _ ->
                val id = values.singleOrNull()?.let { UUID.fromString(it) }
                    ?: throw InternalError("Cannot convert $values to UUID")
                get<RepositoryCitizen>().findById(id, true) ?: throw NotFoundException("Citizen $values not found")
            }
        }

        convert<CitizenRef> {
            decode { values, _ ->
                values.singleOrNull()?.let {
                    CitizenRef(UUID.fromString(it))
                } ?: throw NotFoundException("Citizen $values not found")
            }
        }

        convert<OpinionChoice> {
            decode { values, _ ->
                val id = values.singleOrNull()?.let { UUID.fromString(it) }
                    ?: throw InternalError("Cannot convert $values to UUID")
                get<OpinionChoiceRepository>().findOpinionChoiceById(id)
                    ?: throw NotFoundException("OpinionChoice $values not found")
            }
        }
    }

    install(Locations) {
    }

    install(AuthorizationVoter) {
        voters = mutableListOf(
            ArticleVoter(),
            ConstitutionVoter(),
            CitizenVoter(),
            CommentVoter(),
            VoteVoter(),
            FollowVoter(),
            OpinionVoter(),
            OpinionChoiceVoter()
        )
    }

    HttpClient(Jetty) {
        engine {
        }
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(60) // Disabled (null) by default
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
        masking = false
    }

    install(EventNotification) {
        /* Config Rabbit */
        val exchangeName = config.exchangeNotificationName
        get<ConnectionFactory>().newConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare("push", true, false, false, null)
                channel.queueDeclare("email", true, false, false, null)
                channel.exchangeDeclare(exchangeName, DIRECT, true)
                channel.queueBind("push", exchangeName, "")
                channel.queueBind("email", exchangeName, "")
            }
        }

        /* Declare publisher on event */
        val publisher = Publisher(get(), get())
        subscribe(EntityEvent.Type.UPDATE_ARTICLE.event) {
            publisher.publish(it)
        }

        /* Launch Consumer */
        launch {
            val rabbitChannel = get<ConnectionFactory>().newConnection().createChannel()
            val redis = get<RedisAsyncCommands<String, String>>()

            val consumerPush: Consumer = object : DefaultConsumer(rabbitChannel) {
                @Throws(IOException::class)
                override fun handleDelivery(
                    consumerTag: String,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray
                ) = runBlocking {
                    val message = body.toString(Charsets.UTF_8)
                    val msg = message.deserialize<EntityEvent>() ?: error("Unable to unserialise event message from rabbit")

                    let {
                        when (msg.type) {
                            Notification.Type.ARTICLE -> get<FollowArticle>()
                        } as Follow<*,*>
                    }
                    .findFollowsByTarget(msg.target)
                    .collect { follow ->
                        redis.zadd(
                            "notification:${follow.createdBy.id}",
                            msg.id,
                            message
                        )
                    }

                    rabbitChannel.basicAck(envelope.deliveryTag, false)
                }
            }

            val consumerEmail: Consumer = object : DefaultConsumer(rabbitChannel) {
                @Throws(IOException::class)
                override fun handleDelivery(
                    consumerTag: String,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray
                ) {
                    val message = body.toString(Charsets.UTF_8)
                    println("The message is receive for send email: $message")
                    // TODO implement email sender
                    rabbitChannel.basicAck(envelope.deliveryTag, false)
                }
            }
            rabbitChannel.basicConsume("push", false, consumerPush) // The front consume the redis via Websocket
            rabbitChannel.basicConsume("email", false, consumerEmail)
        }
    }

    install(Authentication) {
        /**
         * Setup the JWT authentication to be used in [Routing].
         * If the token is valid, the corresponding [User] is fetched from the database.
         * The [User] can then be accessed in each [ApplicationCall].
         */
        jwt {
            verifier(JwtConfig.verifier)
            realm = "dc-project.fr"
            validate {
                it.payload.getClaim("id").asString()?.let { id ->
                    get<UserRepository>().findById(UUID.fromString(id))
                }
            }
        }

        jwt("url") {
            verifier(JwtConfig.verifier)
            realm = "dc-project.fr"
            authHeader { call ->
                call.request.queryParameters.get("token")?.let {
                    HttpAuthHeader.Single("Bearer", it)
                }
            }
            validate {
                it.payload.getClaim("id").asString()?.let { id ->
                    get<UserRepository>().findById(UUID.fromString(id))
                }
            }
        }
    }

    install(AutoHeadResponse)

    install(ContentNegotiation) {
        // TODO move to postgresJson lib
        jackson {
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

            registerModule(JodaModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                indentObjectsWith(DefaultIndenter("  ", "\n"))
            })
        }
    }

    install(Routing) {
        // trace { application.log.trace(it.buildText()) }
        authenticate(optional = true) {
            article(get())
            auth(get(), get(), get())
            citizen(get(), get())
            constitution(get())
            followArticle(get())
            followConstitution(get())
            comment(get())
            commentArticle(get())
            commentConstitution(get())
            voteArticle(get(), get(), get())
            voteConstitution(get())
            opinionArticle(get())
            opinionChoice(get())
            definition()
        }

        authenticate("url") {
            notificationArticle(get(), get(named("ws")))
        }
    }

    install(StatusPages) {
        // TODO move to postgresJson lib
        exception<CompletionException> { e ->
            val parent = e.cause?.cause
            if (parent is GenericDatabaseException) {
                call.respond(HttpStatusCode.BadRequest, parent.errorMessage.message!!)
            } else {
                throw e
            }
        }
        exception<NotFoundException> { e ->
            call.respond(HttpStatusCode.BadRequest, e.message!!)
        }
        exception<ForbiddenException> {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        header(HttpHeaders.Authorization)
        anyHost()
        // host("localhost:4200", schemes = listOf("http", "https"))
        allowCredentials = true
        allowSameOrigin = true
        maxAge = Duration.ofDays(1)
    }

    // TODO move to postgresJson lib
    if (env == PROD) {
        get<Migrations>().run()
    }
}
