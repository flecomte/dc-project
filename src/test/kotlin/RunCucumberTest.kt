import feature.KtorServerContext
import fr.dcproject.config
import fr.dcproject.module
import fr.dcproject.utils.LoggerDelegate
import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.cucumber.core.api.Scenario
import io.cucumber.java8.En
import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.get
import org.slf4j.Logger

var unitialized: Boolean = false

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@RunWith(Cucumber::class)
@CucumberOptions(plugin = ["pretty"])
class RunCucumberTest: En, KoinTest {
    private val logger: Logger? by LoggerDelegate()

    val ktorContext = KtorServerContext {
        module()
    }

    init {
        Before(-2) { _: Scenario ->
            if (!unitialized) {
                config.database = "test"
                config.username = "test"
                config.password = "test"

                withTestApplication({ module() }) {
                    migrations()
                    fixtures()
                }
                unitialized = true
            }
        }

        Before(-1) { scenario: Scenario ->
            config.database = "test"
            config.username = "test"
            config.password = "test"
            ktorContext.start()
        }

        After { _: Scenario ->
            ktorContext.stop()
        }
    }

    private fun migrations() {
        config.database = "test"
        config.username = "test"
        config.password = "test"
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
            queriesDirectory = config.sqlFiles.resolve("fixtures")
        ).createRequester()
    }
}
