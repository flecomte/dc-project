package fr.dcproject.application

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.github.jasync.sql.db.postgresql.exceptions.GenericDatabaseException
import component.auth.jwt.jwtInstallation
import fr.dcproject.application.Env.PROD
import fr.dcproject.component.article.routes.findArticleVersions
import fr.dcproject.component.article.routes.findArticles
import fr.dcproject.component.article.routes.getOneArticle
import fr.dcproject.component.article.routes.upsertArticle
import fr.dcproject.component.auth.ForbiddenException
import fr.dcproject.component.auth.routes.authLogin
import fr.dcproject.component.auth.routes.authPasswordless
import fr.dcproject.component.auth.routes.authRegister
import fr.dcproject.component.auth.user
import fr.dcproject.component.citizen.routes.changeMyPassword
import fr.dcproject.component.citizen.routes.findCitizen
import fr.dcproject.component.citizen.routes.getCurrentCitizen
import fr.dcproject.component.citizen.routes.getOneCitizen
import fr.dcproject.component.comment.article.routes.createCommentArticle
import fr.dcproject.component.comment.article.routes.getArticleComments
import fr.dcproject.component.comment.article.routes.getCitizenArticleComments
import fr.dcproject.component.comment.generic.routes.createCommentChildren
import fr.dcproject.component.comment.generic.routes.editComment
import fr.dcproject.component.comment.generic.routes.getChildrenComments
import fr.dcproject.component.comment.generic.routes.getOneComment
import fr.dcproject.component.views.ConfigViews
import fr.dcproject.component.workgroup.routes.CreateWorkgroup.createWorkgroup
import fr.dcproject.component.workgroup.routes.DeleteWorkgroup.deleteWorkgroup
import fr.dcproject.component.workgroup.routes.EditWorkgroup.editWorkgroup
import fr.dcproject.component.workgroup.routes.GetWorkgroup.getWorkgroup
import fr.dcproject.component.workgroup.routes.GetWorkgroups.getWorkgroups
import fr.dcproject.component.workgroup.routes.members.AddMemberToWorkgroup.addMemberToWorkgroup
import fr.dcproject.component.workgroup.routes.members.DeleteMembersOfWorkgroup.deleteMemberOfWorkgroup
import fr.dcproject.component.workgroup.routes.members.UpdateMemberOfWorkgroup.updateMemberOfWorkgroup
import fr.dcproject.event.EventNotification
import fr.dcproject.event.EventSubscriber
import fr.dcproject.routes.*
import fr.dcproject.security.voter.OpinionChoiceVoter
import fr.dcproject.security.voter.OpinionVoter
import fr.ktorVoter.AuthorizationVoter
import fr.ktorVoter.VoterException
import fr.postgresjson.migration.Migrations
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.jetty.Jetty
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.jetty.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.eclipse.jetty.util.log.Slf4jLog
import org.koin.core.qualifier.named
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.slf4j.event.Level
import java.time.Duration
import java.util.concurrent.CompletionException

fun main(args: Array<String>): Unit = EngineMain.main(args)

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
            OpinionVoter(),
            OpinionChoiceVoter()
        )
    }

    HttpClient(Jetty) {
        engine {
        }
    }

    ConfigViews.createEsIndexForViews(get())

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(60) // Disabled (null) by default
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
        masking = false
    }

    install(EventSubscriber) {
        EventNotification(this, get(), get(), get(), get(), get()).config()
    }

    install(Authentication, jwtInstallation(get()))

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
            /* Article */
            findArticles(get(), get())
            getOneArticle(get(), get())
            upsertArticle(get(), get(), get())
            findArticleVersions(get(), get())
            /* Citizen */
            findCitizen(get(), get())
            getOneCitizen(get())
            getCurrentCitizen(get())
            changeMyPassword(get(), get())
            /* Comment */
            editComment(get(), get())
            getOneComment(get(), get())
            createCommentChildren(get(), get())
            getChildrenComments(get(), get())
            /* Comment Article */
            getArticleComments(get(), get())
            createCommentArticle(get(), get())
            getCitizenArticleComments(get(), get())
            /* Auth */
            authLogin(get())
            authRegister(get())
            authPasswordless(get())
            /* Workgroup */
            getWorkgroups(get(), get())
            getWorkgroup(get(), get())
            createWorkgroup(get(), get())
            editWorkgroup(get(), get())
            deleteWorkgroup(get(), get())
            /* Workgroup members */
            addMemberToWorkgroup(get(), get())
            deleteMemberOfWorkgroup(get(), get())
            updateMemberOfWorkgroup(get(), get())
            /* TODO */
            constitution(get(), get())
            followArticle(get(), get())
            followConstitution(get(), get())
            commentConstitution(get(), get())
            voteArticle(get(), get(), get(), get())
            voteConstitution(get(), get())
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
