package functional

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.application.Configuration
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.follow.FollowArticleRepository
import fr.dcproject.component.follow.FollowSimple
import fr.dcproject.event.ArticleUpdate
import fr.dcproject.event.EventNotification
import fr.dcproject.event.publisher.Publisher
import fr.dcproject.messages.NotificationEmailSender
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventNotificationTest : KoinTest, AutoCloseKoinTest() {
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
        val redisClient = spyk<RedisAsyncCommands<String, String>> {
            RedisClient.create(Configuration.redis).connect().async() ?: error("Unable to connect to redis")
        }
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
        val mapper = jacksonObjectMapper().apply {
            registerModule(SimpleModule())
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE

            registerModule(JodaModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        }
        /* Purge rabbit notification queues */
        rabbitFactory.newConnection().createChannel().apply {
            queuePurge("push")
            queuePurge("email")
        }

        /* Config consumer */
        EventNotification(
            rabbitFactory = rabbitFactory,
            redis = redisClient,
            followArticleRepo = followArticleRepo,
            followConstitutionRepo = mockk(),
            notificationEmailSender = emailSender,
            exchangeName = "notification_test",
            mapper = mapper,
        ).config()
        verify { rabbitFactory.newConnection() }

        /* Push message */
        Publisher(
            mapper = mapper,
            factory = rabbitFactory,
            exchangeName = "notification_test",
        ).publish(
            ArticleUpdate(
                ArticleForView(
                    title = "MyTitle",
                    content = "myContent",
                    description = "myDescription",
                    createdBy = CitizenRef()
                )
            )
        ).await()

        /* Wait to receive message */
        delay(300)

        /* Check if notifications sent */
        verify { followArticleRepo.findFollowsByTarget(any()) }
        verify { emailSender.sendEmail(any()) }
        verify { redisClient.zadd(any<String>(), any<Double>(), any<String>()) }
    }
}
