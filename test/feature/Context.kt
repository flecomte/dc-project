package feature

import cucumber.api.Scenario
import fr.dcproject.module
import io.ktor.application.Application
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest

class Context(
    val engine: TestApplicationEngine,
    val scenario: Scenario
) {
    companion object {
        lateinit var current: Context
    }

    init {
        engine.start()
        val moduleFunction: Application.() -> Unit = { module() }
        val test: TestApplicationEngine.() -> Unit = {
            moduleFunction(application)
        }
        engine.test()
    }

    var call: TestApplicationCall? = null

    private val requestContextConfigurations: MutableList<TestApplicationRequest.() -> Unit> = mutableListOf()
    fun setupRequest(testApplicationRequest: TestApplicationRequest) {
        requestContextConfigurations.forEach {
            it(testApplicationRequest)
        }
    }
    fun setupNextRequests(requestContextConfiguration: TestApplicationRequest.() -> Unit) = requestContextConfigurations.add(requestContextConfiguration)
}

fun TestApplicationRequest.applyConfigurations() {
    Context.current.setupRequest(this)
}