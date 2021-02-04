package functional

import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.application.Configuration
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.follow.FollowArticleRepository
import fr.dcproject.component.follow.FollowSimple
import fr.dcproject.messages.NotificationEmailSender
import fr.dcproject.notification.ArticleUpdateNotification
import fr.dcproject.notification.NotificationConsumer
import fr.dcproject.notification.publisher.Publisher
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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Tags(Tag("functional"))
class NotificationConsumerTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun before() {
            val config: Configuration = Configuration("application-test.conf")
            RedisClient.create(config.redis).connect().sync().flushall()

            /* Purge rabbit notification queues */
            ConnectionFactory()
                .apply { setUri(config.rabbitmq) }
                .run {
                    newConnection().createChannel().apply {
                        queuePurge("push")
                        queuePurge("email")
                    }
                }
        }
    }

    @InternalCoroutinesApi
    @KtorExperimentalLocationsAPI
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    @Test
    fun `can be send notification`() = runBlocking {
        val config: Configuration = Configuration("application-test.conf")
        /* Create mocks and spy's */
        val emailSender = mockk<NotificationEmailSender>() {
            every { sendEmail(any()) } returns Unit
        }

        /* Init Spy on redis client */
        val redisClient = spyk<RedisClient>(RedisClient.create(config.redis))
        val asyncCommand = spyk(redisClient.connect().async())
        every { redisClient.connect().async() } returns asyncCommand

        val rabbitFactory: ConnectionFactory = spyk {
            ConnectionFactory().apply { setUri(config.rabbitmq) }
        }
        val followArticleRepo = mockk<FollowArticleRepository> {
            every { findFollowsByTarget(any()) } returns flow {
                FollowSimple(
                    createdBy = CitizenRef(),
                    target = ArticleRef(),
                ).let { emit(it) }
            }
        }

        /* Config consumer */
        val consumer = NotificationConsumer(
            rabbitFactory = rabbitFactory,
            redisClient = redisClient,
            followArticleRepo = followArticleRepo,
            followConstitutionRepo = mockk(),
            notificationEmailSender = emailSender,
            exchangeName = "notification_test",
        ).apply { start() }
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

//        consumer.close()
    }
}
