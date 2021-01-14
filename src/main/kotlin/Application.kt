package fr.dcproject

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException
import fr.dcproject.Env.PROD
import fr.dcproject.component.article.route.findArticleVersions
import fr.dcproject.component.article.route.upsertArticle
import fr.dcproject.component.article.routes.findArticles
import fr.dcproject.component.article.routes.getOneArticle
import fr.dcproject.elasticsearch.configElasticIndexes
import fr.dcproject.entity.User
import fr.dcproject.event.EventNotification
import fr.dcproject.event.EventSubscriber
import fr.dcproject.routes.*
import fr.dcproject.security.voter.*
import fr.ktorVoter.AuthorizationVoter
import fr.ktorVoter.VoterException
import fr.postgresjson.migration.Migrations
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.engine.jetty.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.eclipse.jetty.util.log.Slf4jLog
import org.koin.core.qualifier.named
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.slf4j.event.Level
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletionException
import fr.dcproject.repository.User as UserRepository

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

enum class Env { PROD, TEST, CUCUMBER }

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module(env: Env = PROD) {
    install(Koin) {
        Slf4jLog()
        modules(KoinModule)
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(DataConversion, converters)

    install(Locations)

    install(AuthorizationVoter) {
        voters = listOf(
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

    install(Routing.Feature) {
        // trace { application.log.trace(it.buildText()) }
        authenticate(optional = true) {
            findArticles(get(), get())
            getOneArticle(get(), get())
            upsertArticle(get(), get(), get())
            findArticleVersions(get(), get())
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
        exception<VoterException> {
            if (call.user == null) call.respond(HttpStatusCode.Unauthorized)
            else call.respond(HttpStatusCode.Forbidden)
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
