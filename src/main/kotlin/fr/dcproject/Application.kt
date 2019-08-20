package fr.dcproject

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import fr.dcproject.entity.Article
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.Constitution
import fr.dcproject.routes.*
import fr.postgresjson.migration.Migrations
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import org.eclipse.jetty.util.log.Slf4jLog
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.slf4j.event.Level
import java.util.*
import fr.dcproject.repository.Article as RepositoryArticle
import fr.dcproject.repository.Citizen as RepositoryCitizen
import fr.dcproject.repository.Constitution as RepositoryConstitution

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
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

    install(Authentication) {
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
        article(get())
        citizen(get())
        constitution(get())
        followArticle(get())
        followConstitution(get())
    }

    // TODO move to postgresJson lib
    get<Migrations>().run()
}
