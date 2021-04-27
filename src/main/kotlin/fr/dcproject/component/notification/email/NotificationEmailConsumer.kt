package fr.dcproject.component.notification.email

import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.component.follow.database.FollowArticleRepository
import fr.dcproject.component.follow.database.FollowCitizenRepository
import fr.dcproject.component.follow.database.FollowConstitutionRepository
import fr.dcproject.component.notification.NotificationConsumerAbstract
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NotificationEmailConsumer(
    rabbitFactory: ConnectionFactory,
    followConstitutionRepo: FollowConstitutionRepository,
    followArticleRepo: FollowArticleRepository,
    followCitizenRepo: FollowCitizenRepository,
    private val notificationEmailSender: NotificationEmailSender,
    private val exchangeName: String,
) : NotificationConsumerAbstract(rabbitFactory, followConstitutionRepo, followArticleRepo, followCitizenRepo) {
    private val logger: Logger = LoggerFactory.getLogger(NotificationEmailConsumer::class.qualifiedName)

    fun start() {
        /* Config Rabbit */
        declareQueue(QUEUE_NAME, exchangeName)
        consumeQueue(QUEUE_NAME) { message ->
            notificationEmailSender.sendEmail(message.follow)
            logger.debug("EmailSend to: ${message.follow.createdBy.id}")
        }
    }

    companion object {
        private const val QUEUE_NAME = "email"
    }
}
