package fr.dcproject.entity

import fr.postgresjson.entity.*
import java.util.*

interface ExtraI <T: EntityI<UUID>>:
    EntityI<UUID>,
    EntityCreatedAt,
    EntityCreatedBy<Citizen>{
    var target: T
}

abstract class Extra<T: EntityI<UUID>>(
    id: UUID? = UUID.randomUUID(),
    createdBy: Citizen
):
    ExtraI<T>,
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<Citizen> by EntityCreatedByImp(createdBy)