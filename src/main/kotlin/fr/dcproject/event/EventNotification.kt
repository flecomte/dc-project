package fr.dcproject.event

import fr.dcproject.entity.Article
import fr.postgresjson.entity.immutable.UuidEntity
import io.ktor.application.*
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.DisposableHandle
import org.joda.time.DateTime

abstract class Notification(
    val type: String,
    val createdAt: DateTime = DateTime.now()
)
abstract class EntityEvent(
    val target: UuidEntity,
    type: String,
    val action: String
) : Notification(type) {
    enum class Type(val event: EventDefinition<ArticleUpdate>) {
        UPDATE_ARTICLE(EventDefinition<ArticleUpdate>())
    }
}

class ArticleUpdate(
    target: Article
) : EntityEvent(target, "article", "update")

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
