import cucumber.api.CucumberOptions
import cucumber.api.Scenario
import cucumber.api.java8.En
import cucumber.api.junit.Cucumber
import feature.Context
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import feature.Context.Companion.current as contextCurrent

@RunWith(Cucumber::class)
@CucumberOptions(plugin = ["pretty"])
class RunCucumberTest: En {
    init {
        Before(-1) { scenario: Scenario ->
//            config.database = "dc-projectg-test"
            contextCurrent = Context(TestApplicationEngine(createTestEnvironment()) {}, scenario)
        }

        After { scenario: Scenario ->
            contextCurrent.engine.stop(0L, 0L, TimeUnit.MILLISECONDS)
        }
    }
}
