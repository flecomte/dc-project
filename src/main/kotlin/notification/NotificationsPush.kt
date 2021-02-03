package notification

import com.fasterxml.jackson.core.JsonProcessingException
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.notification.Notification
import io.ktor.routing.Route
import io.lettuce.core.Limit
import io.lettuce.core.Range
import io.lettuce.core.Range.Boundary
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.pubsub.RedisPubSubAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class NotificationsPush (
    val redisClient: RedisClient,
    citizen: CitizenI,
    incoming: Flow<Notification>,
    onRecieve: suspend (Notification) -> Unit,
)
{
    val redis: RedisAsyncCommands<String, String> = redisClient.connect()?.async() ?: error("Unable to connect to redis")
    val key = "notification:${citizen.id}"
    private var score: Double = 0.0

    init {
        /* Mark as read all incoming notifications */
        GlobalScope.launch {
            incoming.collect {
                markAsRead(it)
            }
        }

        /* Get old notification and sent it to websocket */
        runBlocking {
            getNotifications().collect { onRecieve(it) }
        }

        /* Lisen redis event, and sent the new notification into websocket */
        redisClient.connectPubSub()?.run {
            addListener(object : RedisPubSubAdapter<String, String>() {
                /* On new key publish */
                override fun message(pattern: String?, channel: String?, message: String?) {
                    runBlocking {
                        getNotifications().collect {
                            onRecieve(it)
                        }
                    }
                }
            })

            /* Register to the events */
            async()?.psubscribe("__key*__:$key") ?: error("Unable to connect to redis")
        } ?: error("PubSub Fail")
    }

    /* Return flow with all new notifications */
    private fun getNotifications() = flow<Notification> {
        redis
            .zrangebyscoreWithScores(
                key,
                Range.from(
                    Boundary.excluding(score),
                    Boundary.including(Double.POSITIVE_INFINITY)
                ),
                Limit.from(100)
            )
            .get().forEach {
                emit(Notification.fromString(it.value))
                if (it.score > score) score = it.score
            }
    }

    private suspend fun markAsRead(notificationMessage: Notification) = coroutineScope {
        try {
            redis.zremrangebyscore(
                key,
                Range.from(
                    Boundary.including(notificationMessage.id),
                    Boundary.including(notificationMessage.id)
                )
            )
        } catch (e: JsonProcessingException) {
            LoggerFactory.getLogger(Route::class.qualifiedName)
                .error("Unable to deserialize notification")
        }
    }
}