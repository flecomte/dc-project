package fr.dcproject.component.notification

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fr.dcproject.common.entity.Entity
import fr.dcproject.component.article.database.ArticleForView
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicInteger

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ArticleUpdateNotificationMessage::class, name = "article")
)
open class NotificationMessage(
    val type: String,
    val createdAt: DateTime = DateTime.now()
) {
    val id: Double = nextId()

    private fun nextId(): Double {
        return (createdAt.millis.toString() + nextInt().toString()).toDouble()
    }

    override fun toString(): String = mapper.writeValueAsString(this) ?: error("Unable to serialize notification")

    fun toByteArray() = toString().toByteArray()

    companion object {
        private val counter: AtomicInteger = AtomicInteger(1000)
        fun nextInt(): Int {
            counter.compareAndSet(9999, 1000)
            return counter.incrementAndGet()
        }

        val mapper = jacksonObjectMapper().apply {
            registerModule(SimpleModule())
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE

            registerModule(JodaModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        inline fun <reified T : NotificationMessage> fromString(raw: String): T = mapper.readValue(raw)
    }
}

open class EntityNotificationMessage <E : Entity> (
    open val target: E,
    type: String,
    val action: String
) : NotificationMessage(type)

data class ArticleUpdateNotificationMessage(
    override val target: ArticleForView
) : EntityNotificationMessage<ArticleForView>(target, "article", "update")
