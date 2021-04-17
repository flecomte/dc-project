package fr.dcproject.component.notification

import com.rabbitmq.client.BuiltinExchangeType.DIRECT
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.common.utils.consumeQueue
import fr.dcproject.component.follow.database.FollowArticleRepository
import fr.dcproject.component.follow.database.FollowCitizenRepository
import fr.dcproject.component.follow.database.FollowConstitutionRepository
import fr.dcproject.component.follow.database.FollowForView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

abstract class NotificationConsumerAbstract(
    private val rabbitFactory: ConnectionFactory,
    private val followConstitutionRepo: FollowConstitutionRepository,
    private val followArticleRepo: FollowArticleRepository,
    private val followCitizenRepo: FollowCitizenRepository,
) {
    private val rabbitConnection = rabbitFactory.newConnection()
    private val rabbitChannel = rabbitConnection.createChannel()

    fun close() {
        rabbitChannel.close()
        rabbitConnection.close()
    }

    fun declareQueue(queueName: String, exchangeName: String) {
        rabbitFactory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare(queueName, true, false, false, null)
                channel.exchangeDeclare(exchangeName, DIRECT, true)
                channel.queueBind(queueName, exchangeName, "")
            }
        }
    }

    protected fun consumeQueue(queueName: String, callback: DefaultConsumer.(DecodedMessage<*>) -> Unit) =
        rabbitChannel.consumeQueue(queueName) { body ->
            runBlocking {
                followersFromMessage(body) {
                    callback(it)
                }
            }
        }

    protected suspend fun followersFromMessage(body: ByteArray, action: suspend (DecodedMessage<*>) -> Unit) {
        val rawMessage: String = body.toString(Charsets.UTF_8)
        val notification: EntityNotificationMessage<*> = NotificationMessage.fromString(rawMessage)
        val follows = when (notification.type) {
            "article" -> followArticleRepo.findFollowsByTarget(notification.target)
            "constitution" -> followConstitutionRepo.findFollowsByTarget(notification.target)
            "citizen" -> followCitizenRepo.findFollowsByTarget(notification.target)
            else -> error("event '${notification.type}' not implemented")
        }

        follows.collect { action(DecodedMessage(notification, rawMessage, it)) }
    }

    protected class DecodedMessage <E : Entity> (
        val event: EntityNotificationMessage<E>,
        val rawMessage: String,
        val follow: FollowForView<out TargetRef>
    )
}
