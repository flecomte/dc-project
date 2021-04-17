package fr.dcproject.component.notification.push

import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.component.follow.database.FollowArticleRepository
import fr.dcproject.component.follow.database.FollowCitizenRepository
import fr.dcproject.component.follow.database.FollowConstitutionRepository
import fr.dcproject.component.notification.NotificationConsumerAbstract
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NotificationPushConsumer(
    rabbitFactory: ConnectionFactory,
    followConstitutionRepo: FollowConstitutionRepository,
    followArticleRepo: FollowArticleRepository,
    followCitizenRepo: FollowCitizenRepository,
    redisClient: RedisClient,
    private val exchangeName: String,
) : NotificationConsumerAbstract(rabbitFactory, followConstitutionRepo, followArticleRepo, followCitizenRepo) {
    private val redisConnection = redisClient.connect() ?: error("Unable to connect to redis")
    private val redis: RedisAsyncCommands<String, String> = redisConnection.async() ?: error("Unable to connect to redis")
    private val logger: Logger = LoggerFactory.getLogger(NotificationPushConsumer::class.qualifiedName)

    fun start() {
        /* Config Rabbit */
        declareQueue(QUEUE_NAME, exchangeName)
        consumeQueue(QUEUE_NAME) { message ->
            redis.zadd(
                "notification:${message.follow.createdBy.id}",
                message.event.id,
                message.rawMessage
            )
            logger.debug("Notification was transferred to the redis (follower: ${message.follow.createdBy.id})")
        }
    }

    companion object {
        private const val QUEUE_NAME = "push"
    }
}
