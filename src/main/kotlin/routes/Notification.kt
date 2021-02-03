package fr.dcproject.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.notification.Notification
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.Frame.Text
import io.ktor.http.cio.websocket.readText
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Route
import io.ktor.websocket.webSocket
import io.lettuce.core.RedisClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import notification.NotificationsPush

/**
 * Consume Websocket, then remove notification in redis.
 *
 * Sent all notification to websocket.
 */
@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Route.notificationArticle(redisClient: RedisClient) {
    webSocket("/notifications") {
        /* Convert channel of string from websocket, to a flow of Notification object */
        val incomingFlow: Flow<Notification> = incoming.consumeAsFlow()
            .mapNotNull<Frame, Text> { it as? Frame.Text }
            .map { it.readText() }
            .map { Notification.fromString(it) }

        /* Read user notifications in redis then sent it to the websocket */
        NotificationsPush(redisClient, call.citizen, incomingFlow) {
            outgoing.send(Text(it.toString()))
        }
    }
}

