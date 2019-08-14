package fr.dcproject.entity

import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.EntityI
import fr.postgresjson.entity.UuidEntity
import java.util.*

interface ExtraI <T: EntityI<UUID>>:
    EntityI<UUID>,
    EntityCreatedAt {
    var citizen: Citizen
    var target: T
}

abstract class Extra<T: EntityI<UUID>>(
    id: UUID? = UUID.randomUUID(),
    override var citizen: Citizen
):
    ExtraI<T>,
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp()