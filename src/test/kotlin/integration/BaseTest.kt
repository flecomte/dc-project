package integration

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.application.Configuration
import fr.dcproject.application.Env.TEST
import fr.dcproject.application.module
import fr.postgresjson.connexion.Connection
import fr.postgresjson.migration.Migrations
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import org.junit.jupiter.api.AfterAll
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
    }

    private val engine = TestApplicationEngine(createTestEnvironment())

    protected fun <R> withIntegrationApplication(
        test: TestApplicationEngine.() -> R
    ): R {
        return engine.test()
    }

    @BeforeAll
    fun before() {
        engine.start()
        engine.application.module(TEST)
        if (init == false) {
            init = true
            get<Migrations>().run {
                forceAllDown()
                run()
            }
        }
        get<Connection>()
            .sendQuery("start transaction;", listOf())
    }

    @AfterAll
    fun after() {
        get<Connection>()
            .sendQuery("rollback;", listOf())
        engine.stop(0, 0)
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
