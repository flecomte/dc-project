package fr.dcproject.event

import fr.postgresjson.entity.Serializable
import fr.postgresjson.entity.immutable.UuidEntity
import io.ktor.application.*
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.DisposableHandle
import org.joda.time.DateTime
import kotlin.random.Random.Default.nextInt

abstract class Event(
    val type: String,
    val createdAt: DateTime = DateTime.now()
) : Serializable {
    val id: Double = randId(createdAt.millis)

    private fun randId(time: Long): Double {
        return (time.toString() + nextInt(1000, 9999).toString()).toDouble()
    }
}

open class EntityEvent(
    val target: UuidEntity,
    type: String,
    val action: String
) : Event(type)

/**
 * Installation Class
 */
class EventSubscriber {
    class Configuration(private val monitor: ApplicationEvents) {
        private val subscribers = mutableListOf<DisposableHandle>()
        fun <T : Event> subscribe(definition: EventDefinition<T>, handler: EventHandler<T>): DisposableHandle {
            return monitor.subscribe(definition, handler).also {
                subscribers.add(it)
            }
        }
    }

    companion object Feature : ApplicationFeature<Application, Configuration, EventSubscriber> {
        override val key = AttributeKey<EventSubscriber>("EventSubscriber")

        @KtorExperimentalAPI
        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit
        ): EventSubscriber {
            Configuration(pipeline.environment.monitor).apply(configure)
            return EventSubscriber()
        }
    }
}
