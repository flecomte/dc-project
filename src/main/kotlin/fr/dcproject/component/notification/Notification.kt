package fr.dcproject.component.notification

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

open class Notification(
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

        inline fun <reified T : Notification> fromString(raw: String): T = mapper.readValue(raw)
    }
}

open class EntityNotification(
    val target: Entity,
    type: String,
    val action: String
) : Notification(type)

class ArticleUpdateNotification(
    target: ArticleForView
) : EntityNotification(target, "article", "update")
