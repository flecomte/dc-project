package fr.dcproject.notification

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fr.dcproject.component.article.ArticleForView
import fr.postgresjson.entity.UuidEntity
import org.joda.time.DateTime
import kotlin.random.Random

open class Notification(
    val type: String,
    val createdAt: DateTime = DateTime.now()
) {
    val id: Double = randId(createdAt.millis)

    private fun randId(time: Long): Double {
        return (time.toString() + Random.nextInt(1000, 9999).toString()).toDouble()
    }

    override fun toString(): String = mapper.writeValueAsString(this) ?: error("Unable to serialize notification")

    fun toByteArray() = toString().toByteArray()

    companion object {
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
    val target: UuidEntity,
    type: String,
    val action: String
) : Notification(type)

class ArticleUpdateNotification(
    target: ArticleForView
) : EntityNotification(target, "article", "update")
