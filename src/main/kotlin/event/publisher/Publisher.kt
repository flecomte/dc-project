package fr.dcproject.event.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.event.EntityEvent
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Publisher(
    private val mapper: ObjectMapper,
    private val factory: ConnectionFactory,
    private val logger: Logger = LoggerFactory.getLogger(Publisher::class.qualifiedName),
    private val exchangeName: String,
) {
    suspend fun <T : EntityEvent> publish(it: T): Deferred<Unit> = coroutineScope {
        async {
            factory.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    channel.basicPublish(exchangeName, "", null, it.serialize().toByteArray())
                    logger.debug("Publish message ${it.target.id}")
                }
            }
        }
    }

    private fun EntityEvent.serialize(): String {
        return mapper.writeValueAsString(this) ?: error("Unable tu serialize message")
    }
}
