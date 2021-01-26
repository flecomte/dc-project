package fr.dcproject.event

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
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
import fr.dcproject.event.publisher.Publisher
import fr.dcproject.messages.NotificationEmailSender
import io.ktor.application.EventDefinition
import io.ktor.utils.io.errors.IOException
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ArticleUpdate(
    target: ArticleForView
) : EntityEvent(target, "article", "update") {
    companion object {
        val event = EventDefinition<ArticleUpdate>()
    }
}

class EventNotification(
    private val rabbitFactory: ConnectionFactory,
    private val redis: RedisAsyncCommands<String, String>,
    private val followConstitutionRepo: FollowConstitutionRepository,
    private val followArticleRepo: FollowArticleRepository,
    private val notificationEmailSender: NotificationEmailSender,
    private val exchangeName: String,
    mapper: ObjectMapper,
) {
    private val mapper: ObjectMapper = mapper.copy()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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
                decodeEvent(body) {
                    redis.zadd(
                        "notification:${it.follow.createdBy.id}",
                        it.event.id,
                        it.rawEvent
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
                    decodeEvent(body) {
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

    private suspend fun decodeEvent(body: ByteArray, action: suspend (Msg) -> Unit) {
        val rawEvent: String = body.toString(Charsets.UTF_8)
        val event: EntityEvent = mapper.readValue(rawEvent) ?: error("Unable to deserialize event message from rabbit")
        val targets = when (event.type) {
            "article" -> followArticleRepo.findFollowsByTarget(event.target)
            "constitution" -> followConstitutionRepo.findFollowsByTarget(event.target)
            else -> error("event '${event.type}' not implemented")
        }

        targets.collect { action(Msg(event, rawEvent, it)) }
    }

    private class Msg(
        val event: EntityEvent,
        val rawEvent: String,
        val follow: FollowSimple<out TargetRef, CitizenRef>
    )
}
