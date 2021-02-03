package functional

import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.application.Configuration
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.follow.FollowArticleRepository
import fr.dcproject.component.follow.FollowSimple
import fr.dcproject.notification.ArticleUpdateNotification
import fr.dcproject.notification.NotificationConsumer
import fr.dcproject.notification.publisher.Publisher
import fr.dcproject.messages.NotificationEmailSender
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationConsumerTest {
    @InternalCoroutinesApi
    @KtorExperimentalLocationsAPI
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    @Test
    @Tag("functional")
    fun `can be send notification`() = runBlocking {
        /* Create mocks and spy's */
        val emailSender = mockk<NotificationEmailSender>() {
            every { sendEmail(any()) } returns Unit
        }

        /* Init Spy on redis client */
        val redisClient = spyk<RedisClient>(RedisClient.create(Configuration.redis))
        val asyncCommand = spyk(redisClient.connect().async())
        every { redisClient.connect().async() } returns asyncCommand

        val rabbitFactory: ConnectionFactory = spyk {
            ConnectionFactory().apply { setUri(Configuration.rabbitmq) }
        }
        val followArticleRepo = mockk<FollowArticleRepository> {
            every { findFollowsByTarget(any()) } returns flow {
                FollowSimple(
                    createdBy = CitizenRef(),
                    target = ArticleRef(),
                ).let { emit(it) }
            }
        }

        /* Purge rabbit notification queues */
        rabbitFactory.newConnection().createChannel().apply {
            queuePurge("push")
            queuePurge("email")
        }

        /* Config consumer */
        NotificationConsumer(
            rabbitFactory = rabbitFactory,
            redisClient = redisClient,
            followArticleRepo = followArticleRepo,
            followConstitutionRepo = mockk(),
            notificationEmailSender = emailSender,
            exchangeName = "notification_test",
        ).config()
        verify { rabbitFactory.newConnection() }

        /* Push message */
        Publisher(
            factory = rabbitFactory,
            exchangeName = "notification_test",
        ).publish(
            ArticleUpdateNotification(
                ArticleForView(
                    title = "MyTitle",
                    content = "myContent",
                    description = "myDescription",
                    createdBy = CitizenRef()
                )
            )
        ).await()

        /* Check if notifications sent */
        verify(timeout = 1000) { followArticleRepo.findFollowsByTarget(any()) }
        verify(timeout = 1000) { emailSender.sendEmail(any()) }
        verify(timeout = 1000) { asyncCommand.zadd(any<String>(), any<Double>(), any<String>()) }
    }
}
