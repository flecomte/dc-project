package integration

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.application.Configuration
import fr.dcproject.application.Env.TEST
import fr.dcproject.application.module
import fr.postgresjson.connexion.Connection
import fr.postgresjson.migration.Migrations
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.koin.test.KoinTest
import org.koin.test.get

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
abstract class BaseTest : KoinTest {
    companion object {
        private var init = false
        private val config = Configuration("application-test.conf")
        private val redis: RedisCommands<String, String> = RedisClient.create(config.redis).connect().sync()
        private val rabbit: Channel = ConnectionFactory()
            .apply { setUri(config.rabbitmq) }
            .newConnection()
            .createChannel()
        private val engine = TestApplicationEngine(createTestEnvironment())
    }

    protected fun <R> withIntegrationApplication(
        test: TestApplicationEngine.() -> R
    ): R {
        return engine.test()
    }

    public fun TestApplicationEngine.handleGetRequest(uri: String? = null, body: TestApplicationRequest.() -> String): TestApplicationCall {
        val setupOveride: TestApplicationRequest.() -> Unit = {
            method = HttpMethod.Get
            if (uri != null) {
                this.uri = uri
            }
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(body().trimIndent())
        }
        return handleRequest(true, setupOveride)
    }

    public fun TestApplicationEngine.handlePostRequest(uri: String? = null, body: TestApplicationRequest.() -> String): TestApplicationCall {
        val setupOveride: TestApplicationRequest.() -> Unit = {
            method = HttpMethod.Post
            if (uri != null) {
                this.uri = uri
            }
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(body().trimIndent())
        }
        return handleRequest(true, setupOveride)
    }

    @BeforeAll
    fun before() {
        if (init == false) {
            engine.start()
            engine.application.module(TEST)
            init = true
            get<Migrations>().run {
                forceAllDown()
                run()
            }
            get<Connection>()
                .sendQuery("start transaction;", listOf())
        }
    }

    @BeforeEach
    fun beforeEach() {
        redis.flushall()
        /* Purge rabbit notification queues */
        rabbit.run {
            queuePurge("push")
            queuePurge("email")
        }

        get<Connection>()
            .sendQuery("savepoint test_begin;", listOf())
    }

    @AfterEach
    fun afterEach() {
        get<Connection>()
            .sendQuery("rollback to savepoint test_begin;", listOf())
    }
}


fun TestApplicationCall.`should be respond`(status: HttpStatusCode? = null, block: TestApplicationResponse.() -> Unit) {
    if (status != null) {
        response.status().`should be`(status)
    }

    block(response)
}