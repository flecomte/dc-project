package functional

import com.rabbitmq.client.ConnectionFactory
import fr.dcproject.application.Configuration
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.notification.ArticleUpdateNotification
import fr.dcproject.notification.Notification
import io.lettuce.core.RedisClient
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import notification.NotificationsPush
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tags(Tag("functional"))
internal class NotificationsPushTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun before() {
            val config: Configuration = Configuration("application-test.conf")
            RedisClient.create(config.redis).connect().sync().flushall()

            /* Purge rabbit notification queues */
            ConnectionFactory()
                .apply { setUri(config.rabbitmq) }
                .newConnection().createChannel().apply {
                    queuePurge("push")
                    queuePurge("email")
                }
        }
    }

    @Test
    fun `Notification from redis is well catch and return`() = runBlocking {
        val config: Configuration = Configuration("application-test.conf")
        /* Redis client for test */
        val redisClientTest = RedisClient.create(config.redis)

        /* Init Spy on redis client */
        val redisClient = spyk<RedisClient>(RedisClient.create(config.redis))
        val asyncCommand = spyk(redisClient.connect().async())
        every { redisClient.connect().async() } returns asyncCommand

        /* Citizen of notification */
        val citizen = CitizenRef()
        /* Article is the target of the notification */
        val article = ArticleForView(
            content = "content..",
            createdBy = citizen,
            description = "desc",
            title = "Super Title",
        )
        /* Init two notification, one called before subscription, and the other after */
        val notifBeforeSubscribe = ArticleUpdateNotification(article)
        runBlocking {
            delay(100)
        }
        val notifAfterSubscribe = ArticleUpdateNotification(article)

        /* init event for emulate incomint message from websocket */
        val event = MutableSharedFlow<Notification>()
        val incomingFlow = event.asSharedFlow()

        spyk(object { var counter = 0 }).run { /* Counter for count the callback of notification */
            /* Sent notification */
            redisClientTest.connect().run {
                sync().zadd(
                    "notification:${citizen.id}",
                    notifBeforeSubscribe.id,
                    notifBeforeSubscribe.toString()
                )
                close()
            }

            /* Init NotificationPush system, and set assertion in callback */
            val notificationPush = NotificationsPush.Builder(redisClient).build(citizen, incomingFlow) {
                counter++
                if (counter == 1) it.id `should be equal to` notifBeforeSubscribe.id
                else it.id `should be equal to` notifAfterSubscribe.id
            }

            /* Sent the notification */
            redisClientTest.connect().run {
                sync().zadd(
                    "notification:${citizen.id}",
                    notifAfterSubscribe.id,
                    notifAfterSubscribe.toString()
                )
                close()
            }

            /* Verify if the callback is called 2 times */
            verify(exactly = 4, timeout = 200) { counter }
            assertEquals(2, counter, "The notification must be call 2 times")

            /* Emit an event to delete notification */
            event.emit(notifAfterSubscribe)
            /* Verify the "mark as read" is called */
            verify(timeout = 500) { asyncCommand.zremrangebyscore(any(), any()) }
            notificationPush.close()
        }
    }
}
