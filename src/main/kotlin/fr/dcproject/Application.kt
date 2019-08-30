package fr.dcproject

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException
import fr.dcproject.Env.PROD
import fr.dcproject.entity.Article
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.Constitution
import fr.dcproject.entity.User
import fr.dcproject.routes.*
import fr.dcproject.security.voter.*
import fr.postgresjson.migration.Migrations
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import org.eclipse.jetty.util.log.Slf4jLog
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.slf4j.event.Level
import java.util.*
import java.util.concurrent.CompletionException
import fr.dcproject.repository.Article as RepositoryArticle
import fr.dcproject.repository.Citizen as RepositoryCitizen
import fr.dcproject.repository.Constitution as RepositoryConstitution
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
                val id = values.singleOrNull()?.let { UUID.fromString(it) }
                    ?: throw InternalError("Cannot convert $values to UUID")
                get<RepositoryArticle>().findById(id) ?: throw InternalError("Article $values not found")
            }
        }

        convert<Constitution> {
            decode { values, _ ->
                val id = values.singleOrNull()?.let { UUID.fromString(it) }
                    ?: throw InternalError("Cannot convert $values to UUID")
                get<RepositoryConstitution>().findById(id) ?: throw InternalError("Constitution $values not found")
            }
        }

        convert<Citizen> {
            decode { values, _ ->
                val id = values.singleOrNull()?.let { UUID.fromString(it) }
                    ?: throw InternalError("Cannot convert $values to UUID")
                get<RepositoryCitizen>().findById(id) ?: throw InternalError("Citizen $values not found")
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
            FollowVoter()
        )
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
    }

    install(AutoHeadResponse)

    install(ContentNegotiation) {
        // TODO move to postgresJson lib
        jackson {
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

            registerModule(JodaModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                indentObjectsWith(DefaultIndenter("  ", "\n"))
            })
        }
    }

    install(Routing) {
//        trace { application.log.trace(it.buildText()) }
        authenticate(optional = true) {
            article(get())
            auth(get(), get())
            citizen(get())
            constitution(get())
            followArticle(get())
            followConstitution(get())
            comment(get())
            commentArticle(get())
            commentConstitution(get())
            voteArticle(get())
            voteConstitution(get())
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
    }

    // TODO move to postgresJson lib
    if (env == PROD) {
        get<Migrations>().run()
    }
}
