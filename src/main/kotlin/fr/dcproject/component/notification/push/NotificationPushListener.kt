package fr.dcproject.component.notification.push

import com.fasterxml.jackson.core.JsonProcessingException
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.notification.NotificationMessage
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

/**
 * Listen a custom flow to mark as read a message.
 *
 * And listen the redis subscription flow and call a callback when a new message arrives
 */
class NotificationPushListener(
    private val redis: RedisAsyncCommands<String, String>,
    private val redisConnectionPubSub: StatefulRedisPubSubConnection<String, String>,
    citizen: CitizenI,
    incoming: Flow<NotificationMessage>,
    onReceive: suspend (NotificationMessage) -> Unit,
) {
    class Builder(redisClient: RedisClient) {
        private val redisConnection = redisClient.connect() ?: error("Unable to connect to redis")
        private val redisConnectionPubSub = redisClient.connectPubSub() ?: error("Unable to connect to redis PubSub")
        private val redis: RedisAsyncCommands<String, String> = redisConnection.async() ?: error("Unable to connect to redis Async")

        /**
         * Build Listener with citizen, incoming flow and set an outgoing callback
         */
        fun build(
            citizen: CitizenI,
            incoming: Flow<NotificationMessage>,
            onReceive: suspend (NotificationMessage) -> Unit,
        ): NotificationPushListener = NotificationPushListener(redis, redisConnectionPubSub, citizen, incoming, onReceive)

        /**
         * Build NotificationPush with only a WebSocket session
         */
        @ExperimentalCoroutinesApi
        fun build(ws: DefaultWebSocketServerSession): NotificationPushListener {
            /* Convert channel of string from websocket, to a flow of Notification object */
            val incomingFlow: Flow<NotificationMessage> = ws.incoming.consumeAsFlow()
                .mapNotNull<Frame, Text> { it as? Frame.Text }
                .map { it.readText() }
                .map { NotificationMessage.fromString(it) }

            return build(ws.call.citizen, incomingFlow) {
                ws.outgoing.send(Text(it.toString()))
            }.apply {
                ws.outgoing.invokeOnClose { close() }
            }
        }
    }

    /**
     * The key of the SortedSet in Redis which contains all the messages of a user
     */
    private val key = "notification:${citizen.id}"
    /**
     * The last score (a kind of sorted ids) of message
     */
    private var lastScore: Double = 0.0
    /**
     * Configure the listener to listen all new notifications
     */
    private val listener = object : RedisPubSubAdapter<String, String>() {
        /* On new key publish */
        override fun message(pattern: String?, channel: String?, message: String?) {
            runBlocking {
                getNewUnreadNotifications().collect {
                    onReceive(it)
                }
            }
        }
    }

    /**
     * Init the listener and the callback
     */
    init {
        /* Mark as read all incoming notifications */
        GlobalScope.launch {
            incoming.collect {
                it.markAsRead()
            }
        }

        /* Get old notification and sent it to websocket */
        runBlocking {
            getNewUnreadNotifications().collect {
                onReceive(it)
            }
        }

        /* Listen redis event, and sent the new notification into websocket */
        redisConnectionPubSub.run {
            addListener(listener)

            /* Register to the events */
            async()?.psubscribe("__key*__:$key") ?: error("Unable to subscribe to redis events")
        }
    }

    /**
     * Close the redis subscription
     */
    fun close() {
        redisConnectionPubSub.removeListener(listener)
    }

    /**
     * Get All new notification from redis and
     * Return flow with notifications
     *
     * On start, on the first call, this method return all unread notification of the user
     *
     * Internally this method return all messages that greater of the lastScore,
     * then define the lastScore with the score of the last message.
     */
    private fun getNewUnreadNotifications() = flow<NotificationMessage> {
        redis
            .zrangebyscoreWithScores(
                key,
                Range.from(
                    Boundary.excluding(lastScore),
                    Boundary.including(Double.POSITIVE_INFINITY)
                ),
                Limit.from(100)
            )
            .get().forEach {
                /* Build message object from raw string and return it */
                emit(NotificationMessage.fromString(it.value))
                if (it.score > lastScore) lastScore = it.score
            }
    }

    /**
     * Mark one notification as read.
     *
     * Internally, this method remove the message of the SortedSet in redis
     */
    private suspend fun NotificationMessage.markAsRead() = coroutineScope {
        try {
            redis.zremrangebyscore(
                key,
                Range.from(
                    Boundary.including(id),
                    Boundary.including(id)
                )
            )
        } catch (e: JsonProcessingException) {
            LoggerFactory.getLogger(Route::class.qualifiedName)
                .error("Unable to deserialize notification")
        }
    }
}
