package functional

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.ShutdownSignalException
import fr.dcproject.application.Configuration
import fr.dcproject.component.article.database.ArticleForView
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.auth.database.UserCreator
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.follow.database.FollowArticleRepository
import fr.dcproject.component.follow.database.FollowForView
import fr.dcproject.component.notification.ArticleUpdateNotification
import fr.dcproject.component.notification.NotificationConsumer
import fr.dcproject.component.notification.NotificationEmailSender
import fr.dcproject.component.notification.Publisher
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
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Tags(Tag("functional"), Tag("notification"))
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
                        try {
                            queuePurge("push")
                            queuePurge("email")
                        } catch (e: ShutdownSignalException) {
                            LoggerFactory.getLogger(NotificationConsumerTest::class.qualifiedName).run {
                                info("queue not exist")
                            }
                        }
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

        val rabbitFactory: ConnectionFactory = ConnectionFactory().apply { setUri(config.rabbitmq) }
        val followArticleRepo = mockk<FollowArticleRepository> {
            every { findFollowsByTarget(any()) } returns flow {
                FollowForView(
                    createdBy = CitizenCreator(name = CitizenI.Name("John", "Doe"), email = "john.doe@dc-project.com", user = UserCreator(username = "john-doe")),
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
            exchangeName = "notification",
        ).apply { start() }

        /* Push message */
        Publisher(
            factory = rabbitFactory,
            exchangeName = "notification",
        ).publish(
            ArticleUpdateNotification(
                ArticleForView(
                    title = "MyTitle",
                    content = "myContent",
                    description = "myDescription",
                    createdBy = CitizenCreator(
                        name = CitizenI.Name(firstName = "", lastName = ""),
                        email = "",
                        user = UserCreator(username = ""),
                    )
                )
            )
        ).await()

        /* Check if notifications sent */
        verify(timeout = 2000) { followArticleRepo.findFollowsByTarget(any()) }
        verify(timeout = 2000) { emailSender.sendEmail(any()) }
        verify(timeout = 2000) { asyncCommand.zadd(any<String>(), any<Double>(), any<String>()) }

        consumer.close()
    }
}
