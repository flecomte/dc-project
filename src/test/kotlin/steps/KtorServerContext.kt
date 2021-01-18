package steps

import io.ktor.application.Application
import io.ktor.server.engine.stop
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.createTestEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.fail

class KtorServerContext(useByDefault: Boolean = true, val module: Application.() -> Unit) {

    init { if (useByDefault) setDefault() }

    companion object {
        lateinit var defaultServer: KtorServerContext
    }

    private val engine = TestApplicationEngine(createTestEnvironment())

    private data class RequestSetup(val setup: TestApplicationRequest.() -> Unit, val keepSetup: Boolean = true)
    private val preRequestSetup = mutableListOf<RequestSetup>()

    var call: TestApplicationCall? = null

    fun addPreRequestSetup(keepSetup: Boolean = true, hook: TestApplicationRequest.() -> Unit) {
        preRequestSetup.add(RequestSetup(hook, keepSetup))
    }

    fun handleRequest(setup: TestApplicationRequest.() -> Unit) =
        try {
            call = engine.handleRequest {
                preRequestSetup.forEach { it.setup(this) }
                setup(this)
            }
        } catch (e: Throwable) {
            fail("Request fail, $e")
        } finally {
            preRequestSetup.removeAll { !it.keepSetup }
        }

    fun setDefault() {
        defaultServer = this
    }

    fun start() {
        engine.start()
        module(engine.application)
    }

    fun stop() {
        engine.stop(0L, 0L, TimeUnit.MILLISECONDS)
    }
}
