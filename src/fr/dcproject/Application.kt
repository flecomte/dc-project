package fr.dcproject

import fr.dcproject.entity.Article
import fr.dcproject.routes.article
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.gson.gson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import java.util.*
import fr.dcproject.repository.Article as RepositoryArticle

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(Koin) {
//        Slf4jLog()
        modules(Module)
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
        convert<Article> {
            decode { values, _ ->
                val id = values.singleOrNull()?.let { UUID.fromString(it) }
                    ?: throw InternalError("Cannot convert $values to Article")
                get<RepositoryArticle>().findById(id)
            }
        }
    }

    install(Locations) {
    }

    install(Authentication) {
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(Routing) {
        article()
    }
}
