package fr.dcproject.component.notification.routes

import fr.dcproject.component.notification.push.NotificationPushListener
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Route
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Consume Websocket, then remove notification in redis.
 *
 * Sent all notification to websocket.
 */
@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Route.notificationArticle(pushListenerBuilder: NotificationPushListener.Builder) {
    webSocket("/notifications") {
        pushListenerBuilder.build(this)
    }
}
