package fr.dcproject.common.dto

import fr.postgresjson.entity.EntityCreatedAt
import org.joda.time.DateTime

interface CreatedAt {
    val createdAt: DateTime

    class Imp(parent: EntityCreatedAt) : CreatedAt {
        override val createdAt: DateTime = parent.createdAt
    }
}
