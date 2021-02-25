package fr.dcproject.common.dto

import org.joda.time.DateTime
import fr.dcproject.common.entity.CreatedAt as EntityCreatedAt

interface CreatedAt {
    val createdAt: DateTime

    class Imp(parent: EntityCreatedAt) : CreatedAt {
        override val createdAt: DateTime = parent.createdAt
    }
}
