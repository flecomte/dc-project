package fr.dcproject.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.event.Event
import fr.postgresjson.serializer.deserialize
import io.ktor.client.HttpClient
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Route
import io.ktor.websocket.webSocket
import io.lettuce.core.Range
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@KtorExperimentalLocationsAPI
fun Route.notificationArticle(redis: RedisAsyncCommands<String, String>, client: HttpClient) {
    webSocket("/notifications") {
        val citizenId = call.citizen.id

        launch {
            incoming.consumeAsFlow().mapNotNull { it as? Frame.Text }.collect {
                val notificationMessage = it.readText().deserialize<Event>() ?: error("unable to deserialize message")

                redis.zremrangebyscore(
                    "notification:$citizenId",
                    Range.from(
                        Range.Boundary.including(notificationMessage.id),
                        Range.Boundary.including(notificationMessage.id)
                    )
                )
            }
        }

        var score = 0.0
        while (!outgoing.isClosedForSend) {
            val result = redis.zrangebyscoreWithScores(
                "notification:$citizenId",
                Range.from(
                    Range.Boundary.excluding(score),
                    Range.Boundary.including(Double.POSITIVE_INFINITY)
                )
            )

            result.get().forEach {
                outgoing.send(Frame.Text(it.value))
                if (it.score > score) score = it.score
            }
            delay(1000)
        }
    }
}
