package fr.dcproject.component.notification

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.BuiltinExchangeType.DIRECT
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.component.follow.database.FollowArticleRepository
import fr.dcproject.component.follow.database.FollowConstitutionRepository
import fr.dcproject.component.follow.database.FollowForView
import io.ktor.utils.io.errors.IOException
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NotificationConsumer(
    private val rabbitFactory: ConnectionFactory,
    private val redisClient: RedisClient,
    private val followConstitutionRepo: FollowConstitutionRepository,
    private val followArticleRepo: FollowArticleRepository,
    private val notificationEmailSender: NotificationEmailSender,
    private val exchangeName: String,
) {
    private val redisConnection = redisClient.connect() ?: error("Unable to connect to redis")
    private val redis: RedisAsyncCommands<String, String> = redisConnection.async() ?: error("Unable to connect to redis")
    private val rabbitConnection = rabbitFactory.newConnection()
    private val rabbitChannel = rabbitConnection.createChannel()
    private val logger: Logger = LoggerFactory.getLogger(Publisher::class.qualifiedName)

    fun close() {
        rabbitChannel.close()
        rabbitConnection.close()
    }

    fun start() {
        /* Config Rabbit */
        rabbitFactory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare("push", true, false, false, null)
                channel.queueDeclare("email", true, false, false, null)
                channel.exchangeDeclare(exchangeName, DIRECT, true)
                channel.queueBind("push", exchangeName, "")
                channel.queueBind("email", exchangeName, "")
            }
        }

        /* Define Consumer */
        val consumerPush: Consumer = object : DefaultConsumer(rabbitChannel) {
            @Throws(IOException::class)
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: BasicProperties,
                body: ByteArray
            ) = runBlocking {
                followersFromMessage(body) {
                    redis.zadd(
                        "notification:${it.follow.createdBy.id}",
                        it.event.id,
                        it.rawMessage
                    )
                }

                rabbitChannel.basicAck(envelope.deliveryTag, false)
            }
        }

        val consumerEmail: Consumer = object : DefaultConsumer(rabbitChannel) {
            @Throws(IOException::class)
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: BasicProperties,
                body: ByteArray
            ) {
                runBlocking {
                    followersFromMessage(body) {
                        notificationEmailSender.sendEmail(it.follow)
                        logger.debug("EmailSend to: ${it.follow.createdBy.id}")
                    }
                }
                rabbitChannel.basicAck(envelope.deliveryTag, false)
            }
        }
        /* Launch Consumer */
        rabbitChannel.basicConsume("push", false, consumerPush) // The front consume the redis via Websocket
        rabbitChannel.basicConsume("email", false, consumerEmail)
    }

    private suspend fun followersFromMessage(body: ByteArray, action: suspend (DecodedMessage) -> Unit) {
        val rawMessage: String = body.toString(Charsets.UTF_8)
        val notification: EntityNotification = Notification.fromString<EntityNotification>(rawMessage) ?: error("Unable to deserialize notification message from rabbit")
        val follows = when (notification.type) {
            "article" -> followArticleRepo.findFollowsByTarget(notification.target)
            "constitution" -> followConstitutionRepo.findFollowsByTarget(notification.target)
            else -> error("event '${notification.type}' not implemented")
        }

        follows.collect { action(DecodedMessage(notification, rawMessage, it)) }
    }

    private class DecodedMessage(
        val event: EntityNotification,
        val rawMessage: String,
        val follow: FollowForView<out TargetRef>
    )
}
