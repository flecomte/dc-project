package fr.dcproject.common.utils

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import kotlinx.coroutines.runBlocking
import java.io.IOException

fun Channel.consumeQueue(queueName: String, callback: DefaultConsumer.(ByteArray) -> Unit) {
    val consumer: Consumer = object : DefaultConsumer(this) {
        @Throws(IOException::class)
        override fun handleDelivery(
            consumerTag: String,
            envelope: Envelope,
            properties: AMQP.BasicProperties,
            body: ByteArray
        ) = runBlocking {
            try {
                callback(body)
                basicAck(envelope.deliveryTag, false)
            } catch (e: Throwable) {
                basicNack(envelope.deliveryTag, false, true)
            }
        }
    }
    /* Launch Consumer */
    basicConsume(queueName, false, consumer)
}
