package fr.dcproject.component.notification.routes

import fr.dcproject.component.notification.NotificationsPush
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
fun Route.notificationArticle(pushBuilder: NotificationsPush.Builder) {
    webSocket("/notifications") {
        pushBuilder.build(this)
    }
}
