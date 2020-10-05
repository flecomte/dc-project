package fr.dcproject

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException
import fr.dcproject.Env.PROD
import fr.dcproject.elasticsearch.configElasticIndexes
import fr.dcproject.entity.*
import fr.dcproject.event.EventNotification
import fr.dcproject.event.EventSubscriber
import fr.dcproject.routes.*
import fr.dcproject.security.voter.*
import fr.ktorVoter.AuthorizationVoter
import fr.ktorVoter.ForbiddenException
import fr.postgresjson.migration.Migrations
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.eclipse.jetty.util.log.Slf4jLog
import org.koin.core.qualifier.named
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.slf4j.event.Level
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletionException
import fr.dcproject.entity.Workgroup as WorkgroupEntity
import fr.dcproject.repository.Article as RepositoryArticle
import fr.dcproject.repository.Citizen as RepositoryCitizen
import fr.dcproject.repository.Constitution as RepositoryConstitution
import fr.dcproject.repository.OpinionChoice as OpinionChoiceRepository
import fr.dcproject.repository.User as UserRepository
import fr.dcproject.repository.Workgroup as WorkgroupRepository

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

enum class Env { PROD, TEST, CUCUMBER }

@ExperimentalCoroutinesApi
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
            } ?: throw NotFoundException("""UUID "$values" is not valid for Article""")
            }
        }

        convert<CommentRef> {
            decode { values, _ ->
                values.singleOrNull()?.let {
                    CommentRef(UUID.fromString(it))
                } ?: throw NotFoundException("""UUID "$values" is not valid for Comment""")
            }
        }
        convert<ConstitutionRef> {
            decode { values, _ ->
                values.singleOrNull()?.let {
                    ConstitutionRef(UUID.fromString(it))
                } ?: throw NotFoundException("""UUID "$values" is not valid for Constitution""")
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
                get<RepositoryCitizen>().findById(id) ?: throw NotFoundException("Citizen $values not found")
            }
        }

        convert<CitizenRef> {
            decode { values, _ ->
                values.singleOrNull()?.let {
                    CitizenRef(UUID.fromString(it))
                } ?: throw NotFoundException("""UUID "$values" is not valid for Citizen""")
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

        convert<WorkgroupRef> {
            decode { values, _ ->
                values.singleOrNull()?.let {
                    WorkgroupRef(UUID.fromString(it))
                } ?: throw NotFoundException("""UUID "$values" is not valid for Workgroup""")
            }
        }

        convert<WorkgroupEntity> {
            decode { values, _ ->
                val id = values.singleOrNull()?.let { UUID.fromString(it) }
                    ?: throw InternalError("Cannot convert $values to UUID")
                get<WorkgroupRepository>().findById(id)
                    ?: throw NotFoundException("Workgroup $values not found")
            }
        }
    }

    install(Locations) {
    }

    install(AuthorizationVoter) {
        voters = listOf(
            ArticleVoter(get()),
            ConstitutionVoter(),
            CitizenVoter(),
            CommentVoter(),
            VoteVoter(),
            FollowVoter(),
            OpinionVoter(),
            OpinionChoiceVoter(),
            WorkgroupVoter()
        )
    }

    HttpClient(Jetty) {
        engine {
        }
    }

    configElasticIndexes(get())

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(60) // Disabled (null) by default
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
        masking = false
    }

    install(EventSubscriber) {
        EventNotification(this, get(), get(), get(), get(), get()).config()
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
            article(get(), get())
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
            workgroup(get())
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
            call.respond(HttpStatusCode.NotFound, e.message!!)
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

    if (env == PROD) {
        get<Migrations>().run()
    }
}
