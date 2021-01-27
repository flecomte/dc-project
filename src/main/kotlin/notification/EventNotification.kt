package fr.dcproject.notification

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType.DIRECT
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.follow.FollowArticleRepository
import fr.dcproject.component.follow.FollowConstitutionRepository
import fr.dcproject.component.follow.FollowSimple
import fr.dcproject.notification.publisher.Publisher
import fr.dcproject.messages.NotificationEmailSender
import fr.postgresjson.entity.UuidEntity
import io.ktor.utils.io.errors.IOException
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random

open class Notification(
    val type: String,
    val createdAt: DateTime = DateTime.now()
) {
    val id: Double = randId(createdAt.millis)

    private fun randId(time: Long): Double {
        return (time.toString() + Random.nextInt(1000, 9999).toString()).toDouble()
    }

    fun serialize(): String = mapper.writeValueAsString(this) ?: error("Unable to serialize notification")

    fun toByteArray() = serialize().toByteArray()

    companion object {
        val mapper = jacksonObjectMapper().apply {
            registerModule(SimpleModule())
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE

            registerModule(JodaModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        inline fun <reified T : Notification> deserialize(raw: String): T = mapper.readValue(raw)
    }
}

open class EntityNotification(
    val target: UuidEntity,
    type: String,
    val action: String
) : Notification(type)

class ArticleUpdateNotification(
    target: ArticleForView
) : EntityNotification(target, "article", "update")

class EventNotification(
    private val rabbitFactory: ConnectionFactory,
    private val redis: RedisAsyncCommands<String, String>,
    private val followConstitutionRepo: FollowConstitutionRepository,
    private val followArticleRepo: FollowArticleRepository,
    private val notificationEmailSender: NotificationEmailSender,
    private val exchangeName: String,
) {
    private val logger: Logger = LoggerFactory.getLogger(Publisher::class.qualifiedName)

    fun config() {
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
        val rabbitChannel = rabbitFactory.newConnection().createChannel()

        val consumerPush: Consumer = object : DefaultConsumer(rabbitChannel) {
            @Throws(IOException::class)
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: AMQP.BasicProperties,
                body: ByteArray
            ) = runBlocking {
                decodeMessage(body) {
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
                properties: AMQP.BasicProperties,
                body: ByteArray
            ) {
                runBlocking {
                    decodeMessage(body) {
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

    private suspend fun decodeMessage(body: ByteArray, action: suspend (DecodedMessage) -> Unit) {
        val rawMessage: String = body.toString(Charsets.UTF_8)
        val notification: EntityNotification = Notification.deserialize<EntityNotification>(rawMessage) ?: error("Unable to deserialize notification message from rabbit")
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
        val follow: FollowSimple<out TargetRef, CitizenRef>
    )
}
