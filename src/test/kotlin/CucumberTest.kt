import fr.dcproject.application.Configuration
import fr.dcproject.application.Env.CUCUMBER
import fr.dcproject.application.module
import fr.dcproject.utils.LoggerDelegate
import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.cucumber.java8.En
import io.cucumber.java8.Scenario
import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import io.ktor.server.testing.withTestApplication
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

    @InternalCoroutinesApi
    val ktorContext = KtorServerContext {
        module(CUCUMBER)
    }

    init {
        if (!unitialized) {
            Configuration.Database.database = "test"
            Configuration.Database.username = "test"
            Configuration.Database.password = "test"

            withTestApplication({ module(CUCUMBER) }) {
                migrations()
            }
            unitialized = true
        }

        Before(-1) { _: Scenario ->
            Configuration.Database.database = "test"
            Configuration.Database.username = "test"
            Configuration.Database.password = "test"
            ktorContext.start()
            //language=PostgreSQL
            get<Connection>().sendQuery("start transaction;", listOf())
        }

        After { _: Scenario ->
            //language=PostgreSQL
            get<Connection>().sendQuery("rollback;", listOf())
            ktorContext.stop()
        }
    }

    private fun migrations() {
        Configuration.Database.database = "test"
        Configuration.Database.username = "test"
        Configuration.Database.password = "test"
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
            queriesDirectory = Configuration.Sql.fixtureFiles
        ).createRequester()
    }
}
