import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.application.Configuration
import fr.dcproject.application.Env.CUCUMBER
import fr.dcproject.application.module
import fr.dcproject.common.utils.LoggerDelegate
import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.cucumber.java8.En
import io.cucumber.java8.Scenario
import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import io.ktor.server.testing.withTestApplication
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.get
import org.slf4j.Logger
import steps.KtorServerContext

var unitialized: Boolean = false

@InternalCoroutinesApi
@RunWith(Cucumber::class)
@CucumberOptions(plugin = ["pretty"], strict = true)
class CucumberTest : En, KoinTest {
    private val logger: Logger? by LoggerDelegate()
    val config = Configuration("application-test.conf")
    val redis: RedisCommands<String, String> = RedisClient.create(config.redis).connect().sync()
    val rabbit: Channel = ConnectionFactory()
        .apply { setUri(config.rabbitmq) }
        .newConnection()
        .createChannel()

    @InternalCoroutinesApi
    val ktorContext = KtorServerContext {
        module(CUCUMBER)
    }

    init {
        if (!unitialized) {
            withTestApplication({ module(CUCUMBER) }) {
                migrations()
            }
            unitialized = true
        }

        Before(-1) { _: Scenario ->
            ktorContext.start()
            //language=PostgreSQL
            get<Connection>().sendQuery("start transaction;", listOf())
        }

        After { _: Scenario ->
            //language=PostgreSQL
            get<Connection>().sendQuery("rollback;", listOf())

            redis.flushall()
            /* Purge rabbit notification queues */
            rabbit.run {
                queuePurge("push")
                queuePurge("email")
            }

            ktorContext.stop()
        }
    }

    private fun migrations() {
        val migrations: Migrations = get()
        migrations.forceAllDown()
        migrations.run()
    }

    private fun fixtures() {
        logger?.info("Fixtures Begin")

        val connection: Connection = get()
        //language=PostgreSQL
        connection.sendQuery("""truncate table "user" cascade;""")
        //language=PostgreSQL
        connection.sendQuery("""SET fixture.quantity.multiple = '50';""")
        getFixturesRequester()
            .getQueries()
            .sortedBy { it.name }
            .forEach { it.sendQuery() }
        logger?.info("Fixtures Done")
    }

    private fun getFixturesRequester(): Requester {
        return Requester.RequesterFactory(
            connection = get(),
            queriesDirectory = config.sql.fixtureFiles
        ).createRequester()
    }
}
