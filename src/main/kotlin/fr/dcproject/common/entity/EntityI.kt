package fr.dcproject.common.entity

import fr.postgresjson.entity.UuidEntityI
import java.util.UUID

interface EntityI : UuidEntityI {
    override val id: UUID
}

open class Entity(id: UUID? = null) : EntityI {
    override val id: UUID = id ?: UUID.randomUUID()
}
