package fr.dcproject.event

import com.fasterxml.jackson.annotation.JsonValue
import fr.dcproject.entity.Article
import fr.postgresjson.entity.Serializable
import fr.postgresjson.entity.immutable.UuidEntity
import io.ktor.application.*
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.DisposableHandle
import org.joda.time.DateTime
import kotlin.random.Random.Default.nextInt

sealed class NotificationS

open class Notification(
    val type: Type,
    val createdAt: DateTime = DateTime.now()
) : NotificationS(), Serializable {
    val id: Double = randId(createdAt.millis)
    enum class Type(@JsonValue val type: String) {
        ARTICLE("article");
    }

    private fun randId(time: Long): Double {
        return (time.toString() + nextInt(1000, 9999).toString()).toDouble()
    }
}

open class EntityEvent(
    val target: UuidEntity,
    type: Notification.Type,
    val action: String
) : Notification(type) {
    enum class Type(val event: EventDefinition<ArticleUpdate>) {
        UPDATE_ARTICLE(EventDefinition<ArticleUpdate>())
    }
}

class ArticleUpdate(
    target: Article
) : EntityEvent(target, Notification.Type.ARTICLE, "update")

/**
 * Installation Class
 */
class EventNotification {
    class Configuration(private val monitor: ApplicationEvents) {
        private val subscribers = mutableListOf<DisposableHandle>()
        fun <T: Notification> subscribe(definition: EventDefinition<T>, handler: EventHandler<T>): DisposableHandle {
            return monitor.subscribe(definition, handler).also {
                subscribers.add(it)
            }
        }
    }

    companion object Feature : ApplicationFeature<Application, Configuration, EventNotification> {
        override val key = AttributeKey<EventNotification>("EventNotification")

        @KtorExperimentalAPI
        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit
        ): EventNotification {
            Configuration(pipeline.environment.monitor).apply(configure)
            return EventNotification()
        }
    }
}
