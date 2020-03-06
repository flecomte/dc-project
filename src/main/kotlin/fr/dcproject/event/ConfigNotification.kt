package fr.dcproject.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.*
import com.rabbitmq.client.BuiltinExchangeType.DIRECT
import fr.dcproject.config
import fr.dcproject.entity.Article
import fr.dcproject.event.publisher.Publisher
import fr.dcproject.repository.Follow
import fr.postgresjson.serializer.deserialize
import io.ktor.application.EventDefinition
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.errors.IOException
import fr.dcproject.repository.FollowArticle as FollowArticleRepository

class ArticleUpdate(
    target: Article
) : EntityEvent(target, "article", "update") {
    companion object {
        val event = EventDefinition<ArticleUpdate>()
    }
}

fun EventSubscriber.Configuration.configEvent(
    rabbitFactory: ConnectionFactory,
    redis: RedisAsyncCommands<String, String>,
    followRepo: FollowArticleRepository,
    serialiser: ObjectMapper
) {
    /* Config Rabbit */
    val exchangeName = config.exchangeNotificationName
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
    val publisher = Publisher(serialiser, rabbitFactory)
    subscribe(ArticleUpdate.event) {
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
                val message = body.toString(Charsets.UTF_8)
                val msg =
                    message.deserialize<EntityEvent>() ?: error("Unable to unserialise event message from rabbit")

                let {
                    when (msg.type) {
                        "article" -> followRepo
                        else -> error("event '${msg.type}' not implemented")
                    } as Follow<*, *>
                }
                    .findFollowsByTarget(msg.target)
                    .collect { follow ->
                        redis.zadd(
                            "notification:${follow.createdBy.id}",
                            msg.id,
                            message
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
                val message = body.toString(Charsets.UTF_8)
                println("The message is receive for send email: $message")
                // TODO implement email sender
                rabbitChannel.basicAck(envelope.deliveryTag, false)
            }
        }
        rabbitChannel.basicConsume("push", false, consumerPush) // The front consume the redis via Websocket
        rabbitChannel.basicConsume("email", false, consumerEmail)
    }
}
