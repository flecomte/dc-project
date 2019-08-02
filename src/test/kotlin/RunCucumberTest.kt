import cucumber.api.CucumberOptions
import cucumber.api.Scenario
import cucumber.api.java8.En
import cucumber.api.junit.Cucumber
import feature.Context
import fr.dcproject.config
import fr.postgresjson.migration.Migrations
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.concurrent.TimeUnit
import feature.Context.Companion.current as contextCurrent

@RunWith(Cucumber::class)
@CucumberOptions(plugin = ["pretty"])
class RunCucumberTest: En, KoinTest {
    private val migrations: Migrations  by inject()
    init {
        Before(-1) { scenario: Scenario ->
            config.database = "test"
            config.username = "test"
            config.password = "test"
            contextCurrent = Context(TestApplicationEngine(createTestEnvironment()) {}, scenario)

            migrations.run()
        }

        After { scenario: Scenario ->
            migrations.forceAllDown()
            contextCurrent.engine.stop(0L, 0L, TimeUnit.MILLISECONDS)
        }
    }
}
