package fr.dcproject.component.notification

import com.fasterxml.jackson.core.JsonProcessingException
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.citizen.database.CitizenI
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.Frame.Text
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.Route
import io.ktor.websocket.DefaultWebSocketServerSession
import io.lettuce.core.Limit
import io.lettuce.core.Range
import io.lettuce.core.Range.Boundary
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class NotificationsPush private constructor(
    private val redis: RedisAsyncCommands<String, String>,
    private val redisConnectionPubSub: StatefulRedisPubSubConnection<String, String>,
    citizen: CitizenI,
    incoming: Flow<Notification>,
    onRecieve: suspend (Notification) -> Unit,
) {
    class Builder(val redisClient: RedisClient) {
        private val redisConnection = redisClient.connect() ?: error("Unable to connect to redis")
        private val redisConnectionPubSub = redisClient.connectPubSub() ?: error("Unable to connect to redis PubSub")
        private val redis: RedisAsyncCommands<String, String> = redisConnection.async() ?: error("Unable to connect to redis Async")

        fun build(
            citizen: CitizenI,
            incoming: Flow<Notification>,
            onRecieve: suspend (Notification) -> Unit,
        ): NotificationsPush = NotificationsPush(redis, redisConnectionPubSub, citizen, incoming, onRecieve)

        @ExperimentalCoroutinesApi
        fun build(ws: DefaultWebSocketServerSession): NotificationsPush {
            /* Convert channel of string from websocket, to a flow of Notification object */
            val incomingFlow: Flow<Notification> = ws.incoming.consumeAsFlow()
                .mapNotNull<Frame, Text> { it as? Frame.Text }
                .map { it.readText() }
                .map { Notification.fromString(it) }

            return build(ws.call.citizen, incomingFlow) {
                ws.outgoing.send(Text(it.toString()))
            }.apply {
                ws.outgoing.invokeOnClose { close() }
            }
        }
    }

    private val key = "notification:${citizen.id}"
    private var score: Double = 0.0
    private val listener = object : RedisPubSubAdapter<String, String>() {
        /* On new key publish */
        override fun message(pattern: String?, channel: String?, message: String?) {
            runBlocking {
                getNotifications().collect {
                    onRecieve(it)
                }
            }
        }
    }

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
        redisConnectionPubSub.run {
            addListener(listener)

            /* Register to the events */
            async()?.psubscribe("__key*__:$key") ?: error("Unable to subscribe to redis events")
        }
    }

    fun close() {
        redisConnectionPubSub.removeListener(listener)
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
