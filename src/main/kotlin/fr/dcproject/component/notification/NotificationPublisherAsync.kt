package fr.dcproject.component.notification

import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NotificationPublisherAsync(
    private val factory: ConnectionFactory,
    private val logger: Logger = LoggerFactory.getLogger(NotificationPublisherAsync::class.qualifiedName),
    private val exchangeName: String,
) {
    /**
     * Publish a new notification message to RabbitMQ
     */
    suspend fun <T : EntityNotificationMessage<*>> publishAsync(it: T): Deferred<Unit> = coroutineScope {
        async {
            factory.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    channel.basicPublish(exchangeName, "", null, it.toString().toByteArray())
                    logger.debug("Publish message ${it.target.id}")
                }
            }
        }
    }
}
