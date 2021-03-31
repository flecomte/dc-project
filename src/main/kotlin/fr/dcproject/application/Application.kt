package fr.dcproject.application

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import fr.dcproject.application.Env.PROD
import fr.dcproject.application.Env.TEST
import fr.dcproject.application.http.statusPagesInstallation
import fr.dcproject.component.article.articleKoinModule
import fr.dcproject.component.article.routes.installArticleRoutes
import fr.dcproject.component.auth.authKoinModule
import fr.dcproject.component.auth.jwt.jwtInstallation
import fr.dcproject.component.auth.routes.installAuthRoutes
import fr.dcproject.component.citizen.citizenKoinModule
import fr.dcproject.component.citizen.routes.installCitizenRoutes
import fr.dcproject.component.comment.article.routes.installCommentArticleRoutes
import fr.dcproject.component.comment.commentKoinModule
import fr.dcproject.component.comment.constitution.routes.installCommentConstitutionRoutes
import fr.dcproject.component.comment.generic.routes.installCommentRoutes
import fr.dcproject.component.constitution.constitutionKoinModule
import fr.dcproject.component.constitution.routes.installConstitutionRoutes
import fr.dcproject.component.doc.routes.installDocRoutes
import fr.dcproject.component.follow.followKoinModule
import fr.dcproject.component.follow.routes.article.installFollowArticleRoutes
import fr.dcproject.component.follow.routes.constitution.installFollowConstitutionRoutes
import fr.dcproject.component.notification.NotificationConsumer
import fr.dcproject.component.notification.routes.installNotificationsRoutes
import fr.dcproject.component.opinion.opinionKoinModule
import fr.dcproject.component.opinion.routes.installOpinionRoutes
import fr.dcproject.component.views.viewKoinModule
import fr.dcproject.component.vote.routes.installVoteRoutes
import fr.dcproject.component.vote.voteKoinModule
import fr.dcproject.component.workgroup.routes.installWorkgroupRoutes
import fr.dcproject.component.workgroup.workgroupKoinModule
import fr.postgresjson.migration.Migrations
import io.ktor.application.Application
import io.ktor.application.ApplicationStopped
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.client.HttpClient
import io.ktor.client.engine.jetty.Jetty
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.timeout
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.ktor.server.jetty.EngineMain
import io.ktor.util.KtorExperimentalAPI
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.eclipse.jetty.util.log.Slf4jLog
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.slf4j.event.Level
import java.time.Duration

fun main(args: Array<String>): Unit = EngineMain.main(args)

enum class Env { PROD, TEST }

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module(env: Env = PROD) {
    install(Koin) {
        Slf4jLog()
        modules(
            listOf(
                if (env == TEST) module { single { Configuration("application-test.conf") } }
                else module { single { Configuration() } },
                KoinModule,
                articleKoinModule,
                authKoinModule,
                citizenKoinModule,
                commentKoinModule,
                constitutionKoinModule,
                followKoinModule,
                opinionKoinModule,
                viewKoinModule,
                voteKoinModule,
                workgroupKoinModule,
            )
        )
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(DataConversion, converters)

    install(Locations)

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

    get<NotificationConsumer>().run {
        start()
        environment.monitor.subscribe(ApplicationStopped) {
            close()
        }
    }

    install(Authentication, jwtInstallation(get(), get()))

    install(AutoHeadResponse)

    install(ContentNegotiation) {
        jackson {
            propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE

            registerModule(JodaModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(
                DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                }
            )
        }
    }

    install(Routing.Feature) {
        // trace { application.log.trace(it.buildText()) }
        installArticleRoutes()
        installAuthRoutes()
        installCitizenRoutes()
        installCommentArticleRoutes()
        installCommentRoutes()
        installFollowArticleRoutes()
        installFollowConstitutionRoutes()
        installWorkgroupRoutes()
        installOpinionRoutes()
        installVoteRoutes()
        installConstitutionRoutes()
        installCommentConstitutionRoutes()
        installNotificationsRoutes()
        installDocRoutes()
    }

    install(StatusPages, statusPagesInstallation())

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        header(HttpHeaders.Authorization)
        if (env == PROD) {
            host("localhost:4200", schemes = listOf("http", "https"))
        } else {
            anyHost()
        }
        allowCredentials = true
        allowSameOrigin = true
        maxAgeInSeconds = Duration.ofDays(1).seconds
    }

    if (env == PROD) {
        get<Migrations>().run()
    }
}
