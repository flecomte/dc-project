package fr.dcproject.event.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.config
import fr.dcproject.event.EntityEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Publisher(
    private val mapper: ObjectMapper,
    private val factory: ConnectionFactory,
    private val logger: Logger = LoggerFactory.getLogger(Publisher::class.qualifiedName)
) {
    fun <T: EntityEvent>publish(it: T): Job {
        return GlobalScope.launch {
            factory.newConnection().use { connection -> connection.createChannel().use { channel ->
                channel.basicPublish(config.exchangeNotificationName, "", null, it.serialize().toByteArray())
                logger.debug("Publish message ${it.target.id}")
            } }
        }
    }

    private fun EntityEvent.serialize(): String {
        return mapper.writeValueAsString(this) ?: error("Unable tu serialize message")
    }
}
