package fr.dcproject.event

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType.DIRECT
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import fr.dcproject.application.Configuration
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.follow.FollowRepository
import fr.dcproject.component.follow.FollowSimple
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.event.publisher.Publisher
import fr.dcproject.messages.NotificationEmailSender
import fr.postgresjson.serializer.deserialize
import io.ktor.application.ApplicationCall
import io.ktor.application.EventDefinition
import io.ktor.application.application
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.errors.IOException
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import fr.dcproject.component.follow.FollowArticleRepository as FollowArticleRepository

class ArticleUpdate(
    target: ArticleForView
) : EntityEvent(target, "article", "update") {
    companion object {
        val event = EventDefinition<ArticleUpdate>()
    }
}

fun <T : Event> PipelineContext<Unit, ApplicationCall>.raiseEvent(definition: EventDefinition<T>, value: T) =
    application.environment.monitor.raise(definition, value)

class EventNotification(
    private val config: EventSubscriber.Configuration,
    private val rabbitFactory: ConnectionFactory,
    private val redis: RedisAsyncCommands<String, String>,
    private val followRepo: FollowArticleRepository,
    private val publisher: Publisher,
    private val notificationEmailSender: NotificationEmailSender
) {
    private val logger: Logger = LoggerFactory.getLogger(Publisher::class.qualifiedName)

    fun config() {
        /* Config Rabbit */
        val exchangeName = Configuration.exchangeNotificationName
        rabbitFactory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare("push", true, false, false, null)
                channel.queueDeclare("email", true, false, false, null)
                channel.exchangeDeclare(exchangeName, DIRECT, true)
                channel.queueBind("push", exchangeName, "")
                channel.queueBind("email", exchangeName, "")
            }
        }

        /* Declare publisher on event */
        config.subscribe(ArticleUpdate.event) {
            publisher.publish(it)
        }

        /* Launch Consumer */
        GlobalScope.launch {
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
                            "notification:${follow.createdBy.id}",
                            event.id,
                            rawEvent
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
                            logger.debug("EmailSend to: ${follow.createdBy.id}")
                            notificationEmailSender.sendEmail(follow)
                        }
                    }
                    rabbitChannel.basicAck(envelope.deliveryTag, false)
                }
            }
            rabbitChannel.basicConsume("push", false, consumerPush) // The front consume the redis via Websocket
            rabbitChannel.basicConsume("email", false, consumerEmail)
        }
    }

    private suspend fun decodeEvent(body: ByteArray, action: suspend Msg.() -> Unit) {
        val rawEvent = body.toString(Charsets.UTF_8)
        val event = rawEvent.deserialize<EntityEvent>() ?: error("Unable to unserialise event message from rabbit")
        val repo = when (event.type) {
            "article" -> followRepo
            else -> error("event '${event.type}' not implemented")
        } as FollowRepository<*, *>

        repo
            .findFollowsByTarget(event.target)
            .collect {
                Msg(event, rawEvent, it).action()
            }
    }

    private class Msg(
        val event: EntityEvent,
        val rawEvent: String,
        val follow: FollowSimple<out TargetRef, CitizenRef>
    )
}
