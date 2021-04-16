package fr.dcproject.common.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun <T> retry(numOfRetries: Int, duration: Duration = Duration.ZERO, block: (RetryContext) -> T): T {
    val logger: Logger = LoggerFactory.getLogger("fr.dcproject.utils.retry")
    var throwable: Throwable? = null
    for (attempt in 1..numOfRetries) {
        val context = RetryContext()
        try {
            val output = block(context)
            if (context.hasStop()) {
                break
            }
            return output
        } catch (e: Throwable) {
            throwable = e
            logger.debug("Failed attempt $attempt / $numOfRetries. Wait ${duration.inSeconds} seconds")
            Thread.sleep(duration.inMilliseconds.toLong())
        } finally {
            if (context.hasStop()) {
                break
            }
        }
    }
    throw throwable!!
}

class RetryContext() {
    var stoped = false

    fun stop() {
        stoped = true
    }

    fun hasStop(): Boolean = stoped
}
